package com.wavesplatform.lang.benchmark

import java.util.concurrent.ThreadLocalRandom

import com.wavesplatform.lang.benchmark.CryptoBenchmark._
import com.wavesplatform.lang.jvm.Crypto
import org.openjdk.jmh.annotations.Benchmark
import scorex.crypto.signatures.{Curve25519, PrivateKey, PublicKey, Signature}

class CryptoBenchmark extends Crypto {
  @Benchmark
  def sha256_test(): Array[Byte] = sha256(randomBytes(DataBytesLength))

  @Benchmark
  def keccack256_test(): Array[Byte] = keccack256(randomBytes(DataBytesLength))

  @Benchmark
  def blake2b256_test(): Array[Byte] = blake2b256(randomBytes(DataBytesLength))

  @Benchmark
  def curve25519_generateKeypair_test(): (PrivateKey, PublicKey) = curve25519.generateKeypair

  @Benchmark
  def curve25519_sign_test(): Array[Byte] = {
    val (privateKey, _) = curve25519.generateKeypair
    curve25519.sign(privateKey, randomBytes(DataBytesLength))
  }

  @Benchmark
  def curve25519_full_test(): Boolean = {
    val (privateKey, publicKey) = curve25519.generateKeypair
    val message = randomBytes(DataBytesLength)
    val signature = curve25519.sign(privateKey, message)
    Curve25519.verify(Signature @@ signature, message, publicKey)
  }
}

object CryptoBenchmark {
  val DataBytesLength = 512
  val SeedBytesLength = 128

  def randomBytes(length: Int): Array[Byte] = {
    val bytes = Array.fill[Byte](DataBytesLength)(0)
    ThreadLocalRandom.current().nextBytes(bytes)
    bytes
  }

  object curve25519 {
    def generateKeypair: (PrivateKey, PublicKey) = Curve25519.createKeyPair(randomBytes(SeedBytesLength))
    def sign(privateKey: PrivateKey, message: Array[Byte]): Array[Byte] = Curve25519.sign(privateKey, message)
  }
}
