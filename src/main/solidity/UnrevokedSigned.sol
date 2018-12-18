pragma solidity ^0.4.24;

contract UnrevokedSigned {
  bytes32 constant Revoked = hex"ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

  mapping( bytes32 => address[] ) private documentSigners;
  mapping( address => bytes32   ) private signerProfiles;

  function createIdentityForSender( bytes32 profileHash ) public {
    require( signerProfiles[ msg.sender ] == 0, "A signer identity has already been created for this address." );
    signerProfiles[ msg.sender ] = profileHash; 
  }
  function revokeIdentityForSender() public {
    // no requirements here. we conservatively always accept a revocation
    // even of an unknown identity
    signerProfiles[ msg.sender ] = Revoked;
  }
  function isProfileForSigner( address signer, bytes32 profileHash ) public view returns ( bool isProfile ) {
    isProfile = signerProfiles[ signer ] == profileHash;
  }
  function profileHashForSigner( address signer ) public view returns ( bytes32 profileHash ) {
    require( signerProfiles[ msg.sender ] != 0, "No signer identity has been created for this address." );
    profileHash = signerProfiles[ signer ];
  }
  function isValidSigner( address signer ) public view returns ( bool isValid ) {
    bytes32 check = signerProfiles[ signer ];
    isValid = ( check != 0 && check != Revoked );
  }
  function markSigned( bytes32 documentHash ) public {
    require( isValidSigner( msg.sender ), "Only a valid signer can mark a document signed." );
    address[] storage currentSigners = documentSigners[ documentHash ];
    bool check = false;
    uint len = currentSigners.length;
    for ( uint i = 0; i < len; ++i ) {
       if ( currentSigners[i] == msg.sender ) {
          check = true;
	  break;
       }
    }
    if (!check) {
       currentSigners.push( msg.sender );
    }
  }
  function countSigners( bytes32 documentHash ) public view returns ( uint count ) {
    address[] storage allSigners = documentSigners[ documentHash ];
    count = allSigners.length;
  }
  function fetchSigner( bytes32 documentHash, uint index ) public view returns ( address signer, bool valid, bytes32 profileHash ) {
    address[] storage allSigners = documentSigners[ documentHash ];
    signer = allSigners[ index ];
    profileHash = signerProfiles[signer];
    require( profileHash != 0, "We should never find document signers with an unset profile hash!" );
    valid = profileHash != Revoked;
  }
}
