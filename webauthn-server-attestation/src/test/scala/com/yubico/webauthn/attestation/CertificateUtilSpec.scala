package com.yubico.webauthn.attestation

import com.yubico.internal.util.BinaryUtil
import com.yubico.internal.util.CertificateParser
import com.yubico.webauthn.TestAuthenticator
import com.yubico.webauthn.data.ByteArray
import org.bouncycastle.asn1.DEROctetString
import org.junit.runner.RunWith
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import java.security.cert.X509Certificate
import scala.jdk.OptionConverters.RichOptional

@RunWith(classOf[JUnitRunner])
class CertificateUtilSpec extends AnyFunSpec with Matchers {
  describe("parseFidoSerNumExtension") {
    val idFidoGenCeSernum = "1.3.6.1.4.1.45724.1.1.2"

    it("correctly parses the id-fido-gen-ce-sernum extension.") {
      val (cert, _): (X509Certificate, _) = TestAuthenticator
        .generateAttestationCertificate(
          extensions = List(
            (
              idFidoGenCeSernum,
              false,
              new DEROctetString(Array[Byte](0, 1, 2, 3)),
            )
          )
        )

      val result =
        CertificateUtil
          .parseFidoSerNumExtension(cert)
          .toScala
          .map(new ByteArray(_))
      result should equal(Some(ByteArray.fromHex("00010203")))
    }

    it("returns empty when cert has no id-fido-gen-ce-sernum extension.") {
      val (cert, _): (X509Certificate, _) =
        TestAuthenticator.generateAttestationCertificate(extensions = Nil)
      val result =
        CertificateUtil
          .parseFidoSerNumExtension(cert)
          .toScala
      result should be(None)
    }

    it("correctly parses the serial number from a real YubiKey enterprise attestation certificate.") {
      val cert = CertificateParser.parsePem("""-----BEGIN CERTIFICATE-----
        |MIIC8zCCAdugAwIBAgIJAKr/KiUzkKrgMA0GCSqGSIb3DQEBCwUAMC8xLTArBgNV
        |BAMMJFl1YmljbyBGSURPIFJvb3QgQ0EgU2VyaWFsIDQ1MDIwMzU1NjAgFw0yNDA1
        |MDEwMDAwMDBaGA8yMDYwMDQzMDAwMDAwMFowcDELMAkGA1UEBhMCU0UxEjAQBgNV
        |BAoMCVl1YmljbyBBQjEiMCAGA1UECwwZQXV0aGVudGljYXRvciBBdHRlc3RhdGlv
        |bjEpMCcGA1UEAwwgWXViaWNvIEZpZG8gRUUgKFNlcmlhbD0yODI5OTAwMykwWTAT
        |BgcqhkjOPQIBBggqhkjOPQMBBwNCAATImNkI1cwqkW5B3qNrY3pc8zBLhvGyfyfS
        |WCLrODSe8xaRPcZoXYGGwZ0Ua/Hp5nxyD+w1hjS9O9gx8mSDvp+zo4GZMIGWMBMG
        |CisGAQQBgsQKDQEEBQQDBQcBMBUGCysGAQQBguUcAQECBAYEBAGvzvswIgYJKwYB
        |BAGCxAoCBBUxLjMuNi4xLjQuMS40MTQ4Mi4xLjcwEwYLKwYBBAGC5RwCAQEEBAMC
        |AiQwIQYLKwYBBAGC5RwBAQQEEgQQuQ59wTFuT+6iWlamZqZw/jAMBgNVHRMBAf8E
        |AjAAMA0GCSqGSIb3DQEBCwUAA4IBAQAFEMXw1HUDC/TfMFxp2ZrmgQLa5fmzs2Jh
        |C22TUAuY26CYT5dmMUsS5aJd96MtC5gKS57h1auGr2Y4FMxQS9FJHzXAzAtYJfKh
        |j1uS2BSTXf9GULdFKcWvvv50kJ2VmXLge3UgHDBJ8LwrDlZFyISeMZ8jSbmrNu2c
        |8uNBBSfqdor+5H91L1brC9yYneHdxYk6YiEvDBxWjiMa9DQuySh/4a21nasgt0cB
        |prEbfFOLRDm7GDsRTPyefZjZ84yi4Ao+15x+7DM0UwudEVtjOWB2BJtJyxIkXXNF
        |iWFZaxezq0Xt2Kl2sYnMR97ynw/U4TzZDjgb56pN81oKz8Od9B/u
        |-----END CERTIFICATE-----""".stripMargin)

      val result =
        CertificateUtil
          .parseFidoSerNumExtension(cert)
          .toScala
          .map(new ByteArray(_))

      result should equal(Some(ByteArray.fromHex("01AFCEFB")))

      // For YubiKeys, the sernum octet string represents a big-endian integer
      BinaryUtil.getUint32(result.get.getBytes) should be(28299003)
    }
  }
}
