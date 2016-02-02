package scorex.lagonaki.integration

import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import scorex.lagonaki.server.LagonakiApplication
import scorex.utils.{ScorexLogging, untilTimeout}

import scala.concurrent.duration._

class ValidChainGenerationSpecification extends FunSuite with Matchers with BeforeAndAfterAll with ScorexLogging {


  val applications = List(new LagonakiApplication("settings-local1.json"),
    new LagonakiApplication("settings-local2.json"))

  override protected def beforeAll(): Unit = {
    applications.head.run()
    Thread.sleep(5000)
    applications(1).run()
    applications.foreach(_.wallet.generateNewAccounts(10))
    applications.foreach(_.wallet.privateKeyAccounts().nonEmpty shouldBe true)
    applications.foreach(_.blockStorage.history.height() should be > 0)
    log.info("ValidChainGenerationSpecification initialized")
  }

  override protected def afterAll(): Unit = {
    applications.foreach(_.stopAll())
  }


  test("generate 3 blocks") {
    val height = applications.head.blockStorage.history.height()
    untilTimeout(5.minutes, 10.seconds) {
      applications.foreach(_.blockStorage.history.height() should be >= height + 3)
    }
    val last = applications.head.blockStorage.history.lastBlock
    untilTimeout(5.minutes, 10.seconds) {
      applications.head.blockStorage.history.contains(last) shouldBe true
    }
  }

}