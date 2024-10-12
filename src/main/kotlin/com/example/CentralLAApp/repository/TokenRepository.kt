package com.example.CentralLAApp.repository


import com.example.CentralLAApp.entity.auth.Token
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional


@Repository
interface TokenRepository : JpaRepository<Token,Int>{


    @Query(
        """
        select t from Token t inner join User u on t.user.userID = u.userID
        where u.userID = :userId and (t.expired = false or t.revoked = false)
    """
    )
    fun findAllValidTokensByUser(userId : Int) :List<Token>;


    fun findByToken(token: String): Optional<Token>;

}