package com.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.*;
import com.nimbusds.jwt.*;

@Service
@Scope("prototype")
public class JwtService {
	
	private RSAPublicKey pubkey;
	private RSAPrivateKey pvtkey;
	
	
	public JwtService(@Value("${public_jws_cert}") String jws_pub_cert, @Value("${private_jws_cert}") String jws_pvt_cert) {
		try {
			File pubkey_file = new File(jws_pub_cert);
			File pvtkey_file = new File(jws_pvt_cert);
			String pubkey_string = new String(
						Files.readAllBytes(Paths.get(jws_pub_cert)), StandardCharsets.UTF_8
					);
			String pvtkey_string = new String(
					Files.readAllBytes(Paths.get(jws_pvt_cert)), StandardCharsets.UTF_8
					);
			
			pubkey_string = pubkey_string.replaceAll("-----BEGIN PUBLIC KEY-----", "")
										.replaceAll("-----END PUBLIC KEY-----", "")
									      .replaceAll("\\r\\n", "")
									      .replaceAll("[\\r\\n]", "")
									      .replaceAll(System.lineSeparator(), "");
			pvtkey_string = pvtkey_string.replaceAll("-----BEGIN PRIVATE KEY-----", "")
		    		.replaceAll(System.lineSeparator(), "")
		    		.replaceAll("-----END PRIVATE KEY-----", "")
				    .replaceAll("\\r\\n", "")
				    .replaceAll("[\\r\\n]", "");
			
			byte[] pubkey_encoded = Base64.getDecoder().decode(pubkey_string);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubkey_encoded);
		    this.pubkey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
		    
		    byte[] pvtkey_encoded = Base64.getDecoder().decode(pvtkey_string);
		    PKCS8EncodedKeySpec keySpecpvt = new PKCS8EncodedKeySpec(pvtkey_encoded);
		    this.pvtkey= (RSAPrivateKey) keyFactory.generatePrivate(keySpecpvt);
		    System.out.println("JWS Cert initialisation success");
		    System.out.println(this.jweEncrypt("admin"));
		    //System.out.println(encoder.encode("admin"));
		}catch(Exception E) {
			throw new RuntimeException(E);
		}
	}
	
	public String jwsEncrypt(String username) {
		String encryptedUser;
		JWSSigner signer = new RSASSASigner(this.pvtkey);
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
				.subject(username)
				.expirationTime(new Date(System.currentTimeMillis()+1000*60*24))
				.issueTime(new Date(System.currentTimeMillis()))
				.build();
		Payload payload = jwtClaimsSet.toPayload();
		JWSObject jwsObj = new JWSObject(header, payload);
		JWSVerifier verifier = new RSASSAVerifier(this.pubkey);
		
		try {
			jwsObj.sign(signer);
			String encUsername = jwsObj.serialize();
			if(jwsObj.verify(verifier)==true) {
				System.out.println("JWSObject is successful");
				return encUsername;
			} else {
				return "Verifier and Signature does not match. Please check pem keys";
			}
		}catch(Exception E) {
			return E.getMessage();
		}
		
	}
	
	
	public String jwsDecrypt(String encUsername) {
		try {
			SignedJWT signedJwt = SignedJWT.parse(encUsername);
			JWSVerifier verifier = new RSASSAVerifier(this.pubkey);
			if(signedJwt.verify(verifier)) {
				return signedJwt.getJWTClaimsSet().getSubject();
			} else {
				return "Verifier and Signature does not match. Please check pem keys";
			}
		}catch(Exception E) {
			return E.getMessage();
		}
	}
	
	public String jweEncrypt(String username) {
		try {
			JWSSigner signer = new RSASSASigner(this.pvtkey);
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).build();
			JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
					.subject(username)
					.expirationTime(new Date(System.currentTimeMillis()+1000*60*60))
					.issueTime(new Date(System.currentTimeMillis()))
					.build();
			SignedJWT signedJwt = new SignedJWT(header,jwtClaimsSet);
			signedJwt.sign(signer);
			JWEObject jweObject = new JWEObject(
					new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
					.contentType("JWT")
					.build(),
					new Payload(signedJwt)
					);
			jweObject.encrypt(new RSAEncrypter(this.pubkey));
			return jweObject.serialize();
		}catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public String jweDecrypt(String encUsername) {
		try {
			RSADecrypter decrypter = new RSADecrypter(this.pvtkey);
			JWEObject jweObject = JWEObject.parse(encUsername);
			jweObject.decrypt(decrypter);
			SignedJWT signedJwt = jweObject.getPayload().toSignedJWT();
			JWSVerifier verifier = new RSASSAVerifier(this.pubkey);
			if(signedJwt.verify(verifier)) {
				if(isTokenExpired(signedJwt.getJWTClaimsSet()))
					return "Token has expired";
				
				
				return signedJwt.getJWTClaimsSet().getSubject();
			} else {
				return "Verifier and Signature does not match. Please check pem keys";
			}
		}catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public Boolean isJWTtokenExpired(String token) {
		try {
			RSADecrypter decrypter = new RSADecrypter(this.pvtkey);
			JWEObject jweObject = JWEObject.parse(token);
			jweObject.decrypt(decrypter);
			SignedJWT signedJwt = jweObject.getPayload().toSignedJWT();
			JWSVerifier verifier = new RSASSAVerifier(this.pubkey);
			if(signedJwt.verify(verifier)) {
				if(isTokenExpired(signedJwt.getJWTClaimsSet()))
					return false;
				else
					return true;
			} else {
				throw new RuntimeException("Verifier and Signature does not match. Please check pem keys");
			}
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Boolean isTokenExpired(JWTClaimsSet claims) {
		return claims.getExpirationTime().before(new Date(System.currentTimeMillis()));
	}
	
	
//	public static void main(String[] args) {
//		JwtService service=new JwtService("src/main/resources/public_key.pem","src/main/resources/private_key.pem");
//		String encUsername = service.jweEncrypt("SathyaLike5");
//		System.out.println(encUsername);
//		System.out.println(service.jweDecrypt(encUsername));
//		//System.out.println(service.jweDecrypt("eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAtMjU2In0.mI2I68P1_24N2xIF8ie-XnwNjuEwBxALUIU65jODmP9CV-hFTc4g8NSA_HLMXOkf30ODanVd0zHtNZc7clOjIYy2MR4RQshjCMCKx1Sa75j1QRGijg2KqdMLNnQEbETZU0rzuNRGugqkClKTxwJuEJgCfBxIBDNiKhMMEMGK673gcyg05VfBXQ5QAg--Xng213OYzvy3rcmK-Sg4UBzBqiOXBukIaDDssW_6kcM9tv8_LB-pa52VsWOUpJAzQC6ackGW75TIJXTBgahhO3bfDml3GuvzJsvQzDAE0uQ7FNqNabTI3kqH9seapH3nIVZEqIohKx9v9FDTKLk0eM6htw.hyj9OgfwnS4sd7vh.DKTm7MHxRlpJNIyRbe82ViC7Shmj-NzHcEWN--9G6P2nKa1xZ2leAI9gQkpFcUXUGV4kTpIMqm3iPX7QyKF5g14p8ExdvE3Qu7_N_mZC6n259CfUMM1N8--5_R_dG9MqT3c-ym9O023YN6m9MukokoNlrzOqi01wn5_NYf8w6afkcT_xVawC8gzxab2ZaBSYA_6zZHT1DVzV9qODym4vx4XeEA0AVdXG6uNsfxdR85poaOmWn-IizrBcgqlwlh_dqo76idZCsOd9cMXwIciFNIfVJFDNha8rSc1ltc4dCSPYO4AjvkJjHsEtgLyCyn947Vsvnz4k1H0P2mhKPJv-6cCO34qEDVD8uGKqh0S3ph8kBtB5VNldjFsBV-IRl44Ghys1z-UUIQIs2xxVtHiksSMQN3NmfFAEwbNFhP76xufZbfYKYdo0bRqympPJQd6VTn6G9d-xncD_HtzWf689fD4WFKrjcsbuaWjU-HBBhv37RHIZHvVUtyjCexlsMgk2G_VEzfAusMvIjKnAXAWffmfML2XLJ6IGJ-B_NLMR3nF9ghCpwVgpev1Bj7l1OJM4hpe-J4mR.4gCCuzbFtfS0hWa52kWejw"));
//	}

}
