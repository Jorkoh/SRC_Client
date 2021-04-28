package data.remote.utils

import com.squareup.moshi.JsonReader

inline fun JsonReader.readObject(body: () -> Unit) {
    beginObject()
    while (hasNext()) {
        body()
    }
    endObject()
}

inline fun <T : Any> JsonReader.readArrayToList(body: () -> T?): List<T> {
    val result = mutableListOf<T>()
    beginArray()
    while (hasNext()) {
        body()?.let { result.add(it) }
    }
    endArray()
    return result
}

inline fun <T : Any> JsonReader.readObjectToList(body: () -> T?): List<T> {
    val result = mutableListOf<T>()
    beginObject()
    while (hasNext()) {
        body()?.let { result.add(it) }
    }
    endObject()
    return result
}