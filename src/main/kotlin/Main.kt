package com.github.kotlintelegrambot

import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.google.gson.Gson
import java.io.File
import java.net.URL


sealed interface State {
    data object DefaultState: State
    data object WaitingNameState: State
    data class WaitingPhoneState(val name: String): State
    data object WaitingSearthNameState: State
    data object WaitingDeleteNameState: State

}

data class Contact(val name: String, val phone: String)
data class ContactsArray(val contacts: ArrayList<Contact>)

class ContactsRepository {
    private val gson = Gson()
    private val file = File("contacts.json")
    private val contacts = arrayListOf<Contact>()

    fun load() {
        if(!file.exists()) file.createNewFile()


        val text2 = file.readText()
        if(text2.isNotEmpty()) {
            val contacts2 = gson.fromJson(text2, ContactsArray::class.java)
            val contactsArray2: ArrayList<Contact> = contacts2.contacts

            contacts.clear()
            contacts.addAll(contactsArray2)
            println(contacts)
        }
    }

    fun save() {
        val contactsArray = ContactsArray(contacts)
        val text = gson.toJson(contactsArray)
        file.writeText(text)
    }

    fun getAll(): ArrayList<Contact> {
        return contacts
    }

    fun add(name: String,phone: String) {
        contacts.add(Contact(name, phone))
    }

    fun search(query: String): String {
        var rezultat = ""
        val serthRez = contacts.find { it.name == query }


        if (serthRez != null) {
            var rezult = "\n${serthRez.name} " + ":" + " ${serthRez.phone}"
            rezultat = rezult
        }
        else {
            val  notFound = "\nnot found"
            return  notFound

        }
        return rezultat

    }
    fun seeContact(info: String): String {
        var allCon = ""
        var id = 0
        for (con in contacts) {

            var see = "\n" + id.toString() + "  " + con.name + ":" + con.phone
            allCon = allCon + see
            id ++
        }
        return allCon
    }
    fun deleteContact(query: String): String {
        val dc = contacts.get(query.toInt()).name
        return dc
        contacts.removeAt(query.toInt())
        save()

    }
}


fun main() {
    println("Started")
    val contactsRepository = ContactsRepository()
    contactsRepository.load()
    var state: State = State.DefaultState

    val bot = bot {
        token = "7212513557:AAFFNPYpgZNn28R2-uefAJaKUnydKq6T5Ag"
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
                val file = File("D/myBot")
                bot.sendDocument(ChatId.fromId(message.chat.id), file)
            }

            message(filter = Filter.Text) {
                when (state) {
                    is State.DefaultState -> {
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Введите команду:")
                    }
                    is State.WaitingNameState -> {
                        val name = this.message.text.toString()
                        state = State.WaitingPhoneState(name)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Теперь номер:")
                    }
                    is State.WaitingPhoneState -> {
                        val name = (state as State.WaitingPhoneState).name
                        val phone = this.message.text.toString()

                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Добавленый номер: \n$name : $phone")
                        contactsRepository.add(name, phone)
                        contactsRepository.save()
                        state = State.DefaultState
                    }

                    is State.WaitingSearthNameState -> {
                        val name = this.message.text.toString()
                        contactsRepository.search(name)
                        val rez = contactsRepository.search(name)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Ваш номер: $rez")
                        state = State.DefaultState
                    }

                    is State.WaitingDeleteNameState -> {
                        val delConRead = this.message.text.toString()
                        contactsRepository.deleteContact(delConRead)
                        val dc = contactsRepository.deleteContact(delConRead)
                        println(dc)
                        bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Контакт ($dc) удалён!")
                        state = State.DefaultState

                    }


                }
            }
        }
    }
    bot.startPolling()
}

