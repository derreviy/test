package com.github.kotlintelegrambot.repositories

import com.github.kotlintelegrambot.Contact
import com.github.kotlintelegrambot.ContactsArray
import com.google.gson.Gson
import java.io.File

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
            val rezult = "\n${serthRez.name} " + ":" + " ${serthRez.phone}"
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
        for ((id, con) in contacts.withIndex()) {

            val see = "\n" + id.toString() + "  " + con.name + ":" + con.phone
            allCon += see
        }
        return allCon
    }
    fun deleteContact(query: String): String {
        val dc = contacts.get(query.toInt()).name
        contacts.removeAt(query.toInt())
        save()
        return dc
    }
}