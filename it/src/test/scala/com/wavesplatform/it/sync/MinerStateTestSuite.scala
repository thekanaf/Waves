package com.wavesplatform.it.sync

import com.typesafe.config.{Config, ConfigFactory}
import com.wavesplatform.it.api.SyncHttpApi._
import com.wavesplatform.it.sync.transactions.LeaseStatusTestSuiteConfig.blockGenerationOffest
import com.wavesplatform.it.transactions.NodesFromDocker
import com.wavesplatform.it.util._
import org.scalatest.{CancelAfterFailure, FunSuite}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

class MinerStateTestSuite extends FunSuite with CancelAfterFailure with NodesFromDocker {
  import MinerStateTestSuite._

  override protected def nodeConfigs: Seq[Config] = Configs

  private val transferFee    = 0.001.waves
  private val transferAmount = 1000.waves

  private def miner               = nodes.head
  private def nodeWithZeroBalance = nodes.last

  test("node w/o balance can forge blocks after effective balance increase") {
    val (balance1, eff1)    = nodeWithZeroBalance.accountBalances(nodeWithZeroBalance.address)
    val nodeMinerInfoBefore = nodeWithZeroBalance.debugMinerInfo()
    assert(nodeMinerInfoBefore.isEmpty)
    val txId = miner.transfer(miner.address, nodeWithZeroBalance.address, transferAmount, transferFee).id
    nodes.waitForHeightAriseAndTxPresent(txId)

    nodeWithZeroBalance.assertBalances(nodeWithZeroBalance.address, balance1 + transferAmount, eff1 + transferAmount)

    nodeWithZeroBalance.waitForHeight(60, 6.minutes)

    val nodeMinerInfoAfter = nodeWithZeroBalance.debugMinerInfo()
    assert(nodeMinerInfoAfter.nonEmpty)
    println(s"nodeMinerInfoAfter: $nodeMinerInfoAfter")
  }

}

object MinerStateTestSuite {
  import com.wavesplatform.it.NodeConfigs._
  val microblockActivationHeight = 10
  private val minerConfig        = ConfigFactory.parseString(s"""
       |waves {
       |   blockchain {
       |     custom {
       |        functionality {
       |        pre-activated-features = {2=$microblockActivationHeight}
       |        generation-balance-depth-from-50-to-1000-after-height = 100
       |        }
       |        genesis {
       |           signature: "gC84PYfvJRdLpUKDXNddTcWmH3wWhhKD4W9d2Z1HY46xkvgAdqoksknXHKzCBe2PEhzmDW49VKxfWeyzoMB4LKi"
       |           transactions = [
       |              {recipient: "3Hm3LGoNPmw1VTZ3eRA2pAfeQPhnaBm6YFC", amount: 250000000000000},
       |              {recipient: "3HPG313x548Z9kJa5XY4LVMLnUuF77chcnG", amount: 250000000000000},
       |              {recipient: "3HZxhQhpSU4yEGJdGetncnHaiMnGmUusr9s", amount: 250000000000000},
       |              {recipient: "3HVW7RDYVkcN5xFGBNAUnGirb5KaBSnbUyB", amount: 250000000000000}
       |           ]
       |        }
       |     }
       |   }
       |   miner {
       |    quorum = 3
       |    }
       |}
      """.stripMargin)

  private val notMinerConfig = ConfigFactory.parseString(s"""
       |waves.miner.enable=no
       |""".stripMargin).withFallback(minerConfig)

  val Configs: Seq[Config] = Seq(
    minerConfig.withFallback(Default.head),
    minerConfig.withFallback(Default(1)),
    notMinerConfig.withFallback(Default(2)),
    notMinerConfig.withFallback(Default(3)),
    minerConfig.withFallback(Default(4)) // node w/o balance
  )

}
