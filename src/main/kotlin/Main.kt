package com.github.kotlintelegrambot

import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.google.gson.Gson
import java.io.File

const val BOT_TOKEN = "7212513557:AAFFNPYpgZNn28R2-uefAJaKUnydKq6T5Ag"

data class Contact(val name: String, val phone: String)
data class ContactsArray(val contacts: ArrayList<Contact>)

fun main() {
    println("Started")
    val contactsRepository = ContactsRepository()
    contactsRepository.load()
    var state: State = State.DefaultState

    val bot = bot {
        token = BOT_TOKEN // Bot token
        dispatch {

            command("start") {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Нужна помошь? /help")
            }

            command("help") {
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "/c - Просмотр (Не факт что роботает)" + "\n/a - Добавить контакт (Роботает)" + "\n/d - Удалить контакт (Не роботает)" + "\n/s - Поиск контакта (Работает)")
            }

            command("s") {
                if (state !is State.DefaultState) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Нужна помошь? /help")
                    return@command
                }
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Кого ищем?:")
                state = State.WaitingSearthNameState
            }

            command("c") {
                val allCon = contactsRepository.seeContact("")
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = allCon)
            }

            command("a") {
                if (state !is State.DefaultState) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Нужна помошь? \n/help")
                    return@command
                }
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Введите имя:")
                state = State.WaitingNameState
            }

            command("d") {
                if (state !is State.DefaultState) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Нужна помошь? \n/help")
                    return@command
                }

                val delList = contactsRepository.seeContact("")
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Какой номер удалить? (Выберите числом) \n $delList")

                state = State.WaitingDeleteNameState
            }
            command("code"){
                val projectFolder = File(".")
                val archive = Zip.zipFolder(projectFolder, "sources")
                bot.sendDocument(ChatId.fromId(message.chat.id), archive)
            }

            message(filter = Filter.Text) {
                when (state) {
                    is State.DefaultState -> {
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Введите команду:")
                    }
                    is State.WaitingNameState -> {
                        val name = this.message.text.toString()

                        if(!Validator.validateName(name)) {
                            bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Wrong name!")
                            return@message
                        }

                        state = State.WaitingPhoneState(name)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Теперь номер:")
                    }
                    is State.WaitingPhoneState -> {
                        val name = (state as State.WaitingPhoneState).name
                        val phone = this.message.text.toString()

                        if(!Validator.validatePhoneNumber(phone)) {
                            bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Wrong phone, only + and numbers!")
                            return@message
                        }

                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Добавленый номер: \n$name : $phone")
                        contactsRepository.add(name, phone)
                        contactsRepository.save()
                        state = State.DefaultState
                    }

                    is State.WaitingSearthNameState -> {
                        val name = this.message.text.toString()
                        val rez = contactsRepository.search(name)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Ваш номер: $rez")
                        state = State.DefaultState
                    }

                    is State.WaitingDeleteNameState -> {
                        val delConRead = this.message.text.toString()
                        val dc = contactsRepository.deleteContact(delConRead)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Контакт ($dc) удалён!")
                        state = State.DefaultState

                    }


                }
            }
        }
    }
    bot.startPolling()
}

