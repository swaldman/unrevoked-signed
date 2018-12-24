package com.mchange.sc.v1.unrevokedsigned.plugin

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.Def.Initialize

import com.mchange.sc.v1.consuela._ // for toImmutableSeq, we have it in the build classpath via the sbt-ethereum plugin
import com.mchange.sc.v1.consuela.ethereum.{EthAddress,EthHash}
import com.mchange.sc.v1.consuela.ethereum.stub
import com.mchange.sc.v1.consuela.ethereum.stub.sol
import com.mchange.sc.v1.sbtethereum.lib._
import com.mchange.sc.v1.sbtethereum.lib.Parsers._
import sbt.complete.DefaultParsers._

import com.mchange.sc.v1.sbtethereum.SbtEthereumPlugin
import com.mchange.sc.v1.sbtethereum.SbtEthereumPlugin.autoImport._
import com.mchange.sc.v1.unrevokedsigned._
import com.mchange.sc.v1.unrevokedsigned.contract._

object UnrevokedSignedPlugin extends AutoPlugin {

  object autoImport {
    val unrevokedSignedContractAddress = settingKey[String]("The address of the on-blockchain UnrevokedSigned contract. (Be sure to appropriately define ethNodeChainId and ethNodeUrl.)")

    val dataStore = settingKey[DataStore]("Implementation of DataStore trait (see in directory 'project/') in which files should be stored.")

    val createPlaintextProfile = inputKey[EthHash]("Creates a profile for the current sbt-ethereum sender from a given file path, as 'text/plain']")
    val createJsonProfile      = inputKey[EthHash]("Creates a profile for the current sbt-ethereum sender from a given file path, as 'application/json', normalizing to no-space text")
    val createJpegProfile      = inputKey[EthHash]("Creates a profile for the current sbt-ethereum sender from a given file path, as 'image/jpeg'" )
    val createPngProfile       = inputKey[EthHash]("Creates a profile for the current sbt-ethereum sender from a given file path, as 'image/png'" )

    val storeSignPlaintextDocument = inputKey[EthHash]("Creates a document marked signed by the current sbt-ethereum sender from a given file path, as 'text/plain']")
    val storeSignJsonDocument      = inputKey[EthHash]("Creates a document marked signed by the current sbt-ethereum sender from a given file path, as 'application/json', normalizing to no-space text")
    val storeSignJpegDocument      = inputKey[EthHash]("Creates a document marked signed by the current sbt-ethereum sender from a given file path, as 'image/jpeg'" )
    val storeSignPngDocument       = inputKey[EthHash]("Creates a document marked signed by the current sbt-ethereum sender from a given file path, as 'image/png'" )

    val profileForSigner = inputKey[EthHash]("Finds the profile document for a given signer (Ethereum address).")
  }

  import autoImport._

  lazy val ethDefaults : Seq[sbt.Def.Setting[_]] = Seq(
    Compile / createPlaintextProfile := { createProfile( "text/plain" )( Compile ).evaluated },
    Compile / createJsonProfile := { createProfile( "application/json" )( Compile ).evaluated },
    Compile / createJpegProfile := { createProfile( "image/jpeg" )( Compile ).evaluated },
    Compile / createPngProfile := { createProfile( "image/png" )( Compile ).evaluated },
    Compile / storeSignPlaintextDocument := { storeSignDocument( "text/plain" )( Compile ).evaluated },
    Compile / storeSignJsonDocument := { storeSignDocument( "application/json" )( Compile ).evaluated },
    Compile / storeSignJpegDocument := { storeSignDocument( "image/jpeg" )( Compile ).evaluated },
    Compile / storeSignPngDocument := { storeSignDocument( "image/png" )( Compile ).evaluated },
    Compile / profileForSigner := { findProfileForSigner( Compile ).evaluated }
  )

  def createProfile( contentType : String )( config : Configuration ) : Initialize[InputTask[EthHash]] = Def.inputTask {
    val log = streams.value.log
    val store = dataStore.value

    val contractAddress = unrevokedSignedContractAddress.value

    implicit val ( sctx, ssender ) = (config / xethStubEnvironment).value

    val filePath = (token(Space.+) ~> token( NotSpace ).examples("<file-path-to-profile>")).parsed

    val profileBytes = {
      import java.nio.file._
      Files.readAllBytes( Paths.get(filePath) ).toImmutableSeq
    }
    val stub = UnrevokedSigned( contractAddress )

    val hash = store.put( contentType, profileBytes ).assert
    stub.transaction.createIdentityForSender( sol.Bytes32( hash.bytes ) )

    log.info( s"The document at path '${filePath}' has been stored, and defined as the profile for sender address '0x${ssender.address}' on contract at '0x${contractAddress}'." )
    hash
  }

  def storeSignDocument( contentType : String )( config : Configuration ) : Initialize[InputTask[EthHash]] = Def.inputTask {
    val log = streams.value.log
    val store = dataStore.value

    val contractAddress = unrevokedSignedContractAddress.value

    implicit val ( sctx, ssender ) = ( config / xethStubEnvironment ).value

    val filePath = (token(Space.+) ~> token( NotSpace ).examples("<file-path-to-document>")).parsed

    val documentBytes = {
      import java.nio.file._
      Files.readAllBytes( Paths.get(filePath) ).toImmutableSeq
    }
    val stub = UnrevokedSigned( contractAddress )

    val hash = store.put( contentType, documentBytes ).assert
    stub.transaction.markSigned( sol.Bytes32( hash.bytes ) )

    log.info( s"The document at path '${filePath}' has been stored, and is marked signed for sender address '0x${ssender.address}' on contract at '0x${contractAddress}'." )
    hash
  }

  def findProfileForSigner( config : Configuration ) : Initialize[InputTask[EthHash]] = {
    val parserGen = parserGeneratorForAddress( "<signer-address>" )
    val parser = Defaults.loadForParser( config / xethFindCacheRichParserInfo )( parserGen )

    Def.inputTask {
      val log = streams.value.log
      val store = dataStore.value

      val contractAddress = unrevokedSignedContractAddress.value

      implicit val ( sctx, ssender ) = ( config / xethStubEnvironment ).value

      val signerAddress = parser.parsed

      val stub = UnrevokedSigned( contractAddress )

      val profileHash = EthHash.withBytes( stub.constant.profileHashForSigner( signerAddress ).widen )
      val ( contentType, profileBytes ) = store.get( profileHash ).assert

      println( s"Content-Type: ${contentType}" )
      println()

      contentType match {
        case "text/plain" => println( new String( profileBytes.toArray, java.nio.charset.StandardCharsets.UTF_8 ) )
        case _            => println( s"0x${profileBytes.hex}" )
      }

      profileHash
    }
  }

  // plug-in setup

  // very important to ensure the ordering of definitions,
  // so that JvmPlugin's compile actually gets overridden

  override def requires = JvmPlugin && SbtEthereumPlugin

  override def trigger = allRequirements

  override def projectSettings = ethDefaults
}
