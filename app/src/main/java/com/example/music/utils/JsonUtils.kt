package com.example.music.utils

import android.content.Context
import android.util.JsonReader
import com.example.music.dto.MusicItem
import com.example.music.dto.MusicList
import java.io.IOException
import java.io.InputStreamReader

object JsonUtils {

    @Throws(IOException::class)
    private fun readJsonFromAsset(context: Context): JsonReader {
        val assetManager = context.assets
        var fileName: String? = null

        for (asset in assetManager.list("")!!) {
            if (asset.endsWith(".exolist.json")) {
                fileName = asset
                break  // 원하는 파일을 찾으면 루프를 종료
            }
        }
        if (fileName == null) {
            throw IOException("No JSON file")
        }
        // 찾은 파일을 열어 JsonReader를 반환
        val inputStream = assetManager.open(fileName)
        val inputStreamReader = InputStreamReader(inputStream)
        return JsonReader(inputStreamReader)
    }

    @Throws(IOException::class)
    fun readJsonArray(context: Context): List<MusicList> {
        val musicLists = mutableListOf<MusicList>()

        val reader = readJsonFromAsset(context)
        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "list" -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        musicLists.add(readCategoryObject(reader))
                    }
                    reader.endArray()
                }

                else -> reader.skipValue()
            }
        }

        reader.endObject()
        reader.close()

        return musicLists
    }

    private fun readCategoryObject(reader: JsonReader): MusicList {
        var category: String? = ""
        var description: String? = ""
        val items = mutableListOf<MusicItem>()

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "category" -> category = reader.nextString()
                "description" -> description = reader.nextString()
                "items" -> items.addAll(readItemsArray(reader))
                else -> reader.skipValue()  //예상하지 못한 키는 무시
            }
        }

        reader.endObject()

        return MusicList(category, description, items)
    }

    @Throws(IOException::class)
    private fun readItemsArray(reader: JsonReader): List<MusicItem> {
        val musicList = mutableListOf<MusicItem>()

        reader.beginArray() //배열 시작

        while (reader.hasNext()) {
            musicList.add(readItemObject(reader))
        }
        reader.endArray()  //배열 끝
        return musicList
    }

    @Throws(IOException::class)
    private fun readItemObject(reader: JsonReader): MusicItem {
        var id = 0
        var title: String? = ""
        var singer: String? = ""
        var uri: String? = ""
        var image: String? = ""

        reader.beginObject() //배열 시작

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextInt()
                "title" -> title = reader.nextString() ?: ""
                "singer" -> singer = reader.nextString() ?: ""
                "uri" -> uri = reader.nextString() ?: ""
                "image" -> image = reader.nextString() ?: ""
                else -> reader.skipValue()
            }
        }

        reader.endObject()  //배열 끝

        return MusicItem(id, title, singer, uri, image)
    }

}