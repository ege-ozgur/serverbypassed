package com.example.CentralLAApp.service.helper


import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.xml.XmlMapper


object XmlToJsonConverter {
    fun convert(xml: String): JsonNode {
        val xmlMapper = XmlMapper()
        val jsonNode: JsonNode = xmlMapper.readTree(xml)
        return jsonNode
    }
}

