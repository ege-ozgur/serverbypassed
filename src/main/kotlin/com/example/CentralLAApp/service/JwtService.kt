package com.example.CentralLAApp.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import java.util.function.Function


@Service
class JwtService {
    private val SECRET_KEY :String = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
    private var jwtExpiration: Long = 1000*60*60 // 1 hour
    //private var jwtExpiration: Long = 1000*5 // 5 seconds
    private var refreshExpiration: Long = 604800000
    fun extractUsername(token: String): String? {
        return extractClaim(token,Claims::getSubject)
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims: Claims = extractAllClaims(token)
        return claimsResolver.apply(claims)

    }

    fun generateToken(
        userDetails: UserDetails
    ):String{
        return generateToken(HashMap(),userDetails)
    }

    fun generateToken(
        extraClaims: Map<String,Any>,
        userDetails: UserDetails
    ):String{
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis()+
                    jwtExpiration
            ))
            .signWith(getSignInKey(),SignatureAlgorithm.HS256)
            .compact();
    }

    fun generateRefreshToken(
        userDetails: UserDetails
    ): String {
        return buildToken(HashMap(), userDetails, refreshExpiration)
    }

    private fun buildToken(
        extraClaims: Map<String, Any?>,
        userDetails: UserDetails,
        expiration: Long
    ): String {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    fun isTokenValid(token :String,userDetails: UserDetails): Boolean {
        val username :String = extractUsername(token)!!
        return (username == userDetails.username && !isTokenExpired(token))


    }

    private fun isTokenExpired(token: String): Boolean {
        return  extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token,Claims::getExpiration)

    }

    fun  extractAllClaims(token: String): Claims{
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun getSignInKey(): Key {
        val keyBytes : ByteArray? = Decoders.BASE64.decode(SECRET_KEY)
        return Keys.hmacShaKeyFor(keyBytes)
    }

}
