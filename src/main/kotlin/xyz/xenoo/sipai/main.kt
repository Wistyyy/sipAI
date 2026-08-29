package xyz.xenoo.sipai

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val client = HttpClient.newHttpClient()

    // data
    val date = LocalDate.now()
    val time = LocalDateTime.now()

    //sysprompt
    var systemPrompt = "You are a onetime use agent. You answer the Question without any filler words. No after Questions. If the User answers in a way that requires a Question on your side just ignore it and say you can't do it. Start and end your messages with ◇◇◇. Here are some information's: Current date: ${date}, Current time: ${time}. This is the users Question:"

    var model = "deepseek-v4-flash"
    var reasoning = "low"
    var thinking = """
        "thinking": {
            "type": "disabled"
        }
    """.trimIndent()
    var verbose = false

    var i = 0
    val questionParts = mutableListOf<String>()
    while (i < args.size) {

        when (args[i]) {
            "--cleanSystemPrompt" -> systemPrompt = "empty sysprompt"

            "--model" -> {
                when (args[i + 1]) {
                    "flash" -> model = "deepseek-v4-flash"
                    "pro" -> model = "deepseek-v4-pro"
                    "flash-vision" -> model = "deepseek-v4-flash-vision-exp"
                }
                i++
            }

            "--reasoning" -> {
                when (args[i + 1]) {
                    "low" -> reasoning = "low"
                    "high" -> reasoning = "high"
                    "max" -> reasoning = "max"
                }

                thinking = """
                        "thinking": {
                            "type": "enabled"
                    },
                    "reasoning_effort": "$reasoning",
                    "max_tokens": 4096,
                    "response_format": {
                        "type": "text"
                    }
                     """.trimIndent()
                i++
            }

            "--verbose" -> verbose = true

            else -> questionParts.add(args[i])
        }
        i++
    }



    val jsonBody = """
        {
            "model": "$model",
            "messages": [
                {"role": "system", "content": "$systemPrompt"},
                {"role": "user", "content": "${questionParts.joinToString(" ")}"}
            ],
            $thinking
        }
    """.trimIndent()

    val request =
        HttpRequest.newBuilder()
        .uri(URI.create("https://api.deepseek.com/chat/completions"))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer ${System.getenv("DEEPSEEK_API_KEY")}")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build()


    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() != 200) {
        println("Error: ${response.statusCode()}")
        println(response.body())
        exitProcess(1)
    }


    val markerStart = response.body().indexOf("◇◇◇")
    val textStart = markerStart + "◇◇◇".length
    val markerEnd = response.body().indexOf("◇◇◇", textStart)

    val answer = response.body().substring(textStart, markerEnd)
    println("SipAI: $answer")
    if (verbose) {
        println("$model, $reasoning")
    }




}