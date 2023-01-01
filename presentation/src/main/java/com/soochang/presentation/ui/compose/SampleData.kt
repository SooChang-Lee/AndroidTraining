package com.soochang.presentation.ui.compose

data class Message(
    val author : String,
    val body : String
)

object SampleData {
    // Sample conversation data
    val conversationSample = listOf(
        Message(
            "이수창",
            "Test...Test...Test..."
        ),
        Message(
            "이수창",
            "List of Android versions:\n" +
                    "Android KitKat (API 19)\n" +
                    "Android Lollipop (API 21)\n" +
                    "Android Marshmallow (API 23)\n" +
                    "Android Nougat (API 24)\n" +
                    "Android Oreo (API 26)\n" +
                    "Android Pie (API 28)\n" +
                    "Android 10 (API 29)\n" +
                    "Android 11 (API 30)\n" +
                    "Android 12 (API 31)\n"
        ),
        Message(
            "이수창",
            "I think Kotlin is my favorite programming language.\n" +
                    "It's so much fun!"
        ),
        Message(
            "이수창",
            "Searching for alternatives to XML layouts..."
        ),
        Message(
            "이수창",
            "Hey, take a look at Jetpack Compose, it's great!\n" +
                    "It's the Android's modern toolkit for building native UI." +
                    "It simplifies and accelerates UI development on Android." +
                    "Less code, powerful tools, and intuitive Kotlin APIs :)"
        ),
        Message(
            "이수창",
            "It's available from API 21+ :)"
        ),
        Message(
            "이수창",
            "Writing Kotlin for UI seems so natural, Compose where have you been all my life?"
        ),
        Message(
            "이수창",
            "Android Studio next version's name is Arctic Fox"
        ),
        Message(
            "이수창",
            "Android Studio Arctic Fox tooling for Compose is top notch ^_^"
        ),
        Message(
            "이수창",
            "I didn't know you can now run the emulator directly from Android Studio"
        ),
        Message(
            "이수창",
            "Compose Previews are great to check quickly how a composable layout looks like"
        ),
        Message(
            "이수창",
            "Previews are also interactive after enabling the experimental setting"
        ),
        Message(
            "이수창",
            "Have you tried writing build.gradle with KTS?"
        ),
    )
}