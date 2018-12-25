package com.mchange.sc.v1

import com.mchange.sc.v1.consuela._ 
import com.mchange.sc.v1.consuela.ethereum.EthHash

package object unrevokedsigned {
  final object Hash {
    val Zero    = EthHash.withBytes( Array.fill(32)(0.toByte) )
    val Revoked = EthHash.withBytes( "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".decodeHex )
  }
}
