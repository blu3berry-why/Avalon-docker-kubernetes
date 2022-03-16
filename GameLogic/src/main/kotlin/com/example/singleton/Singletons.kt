package com.example.singleton

import com.example.helpers.Constants
import com.example.model.db.Lobby
import com.example.model.db.User
import org.litote.kmongo.reactivestreams.*
import org.litote.kmongo.coroutine.*
import org.mindrot.jbcrypt.BCrypt
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

object Singletons {

    val db by lazy {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val rootLogger = loggerContext.getLogger("org.mongodb.driver")
        rootLogger.level = Level.OFF
        KMongo.createClient(/*
        This is where I would read the connection string but leaving it empty it should run on localhost
        HoconApplicationConfig(ConfigFactory.load()).property("mongo.connection").getString()*/
        "mongodb://mongo:27017/docker_mongo"
        ).coroutine.getDatabase("Archimedes-Rest") }
    val Lobbies by lazy { db.getCollection<Lobby>() }
    val Users by lazy { db.getCollection<User>() }
    val crypt by lazy { BCrypt() }
    val const by lazy { Constants() }
}