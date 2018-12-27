package com.mchange.sc.v1

import com.mchange.sc.v1.consuela._ 
import com.mchange.sc.v1.consuela.ethereum.EthHash

import play.api.libs.json.{Json => PJson}

package object unrevokedsigned {
  final object Hash {
    val Zero    = EthHash.withBytes( Array.fill(32)(0.toByte) )
    val Revoked = EthHash.withBytes( "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".decodeHex )
  }
  final object Normalizer {
    def forContentType( contentType : String ) : Option[Array[Byte] => Array[Byte]] = {
      if ( contentType.startsWith("application/json") || contentType.startsWith("text/json") ) Some(Json) else None
    }
    def forFilePath( filePath : String ) : Option[Array[Byte] => Array[Byte]] = {
      if ( filePath.toLowerCase().endsWith( ".json" ) ) Some(Json) else None
    }
    def choose( mbContentType : Option[String], mbFilePath : Option[String] ) : Array[Byte] => Array[Byte] = {
      val mbFound = mbContentType.flatMap( forContentType ) orElse mbFilePath.flatMap( forFilePath )
      mbFound.getOrElse( identity )
    }
    val Json : Array[Byte] => Array[Byte] = { arr =>
      PJson.toBytes( PJson.parse( arr ) )
    }
  }
}
