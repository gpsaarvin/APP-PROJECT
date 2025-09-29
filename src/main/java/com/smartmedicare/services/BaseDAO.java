package com.smartmedicare.services;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

public abstract class BaseDAO<T> {
    protected final MongoCollection<Document> collection;

    protected BaseDAO(String collectionName) {
        this.collection = DatabaseService.getInstance().getDatabase().getCollection(collectionName);
    }

    public InsertOneResult insert(Document document) {
        return collection.insertOne(document);
    }

    public UpdateResult update(ObjectId id, Document update) {
        return collection.updateOne(Filters.eq("_id", id), new Document("$set", update));
    }

    public DeleteResult delete(ObjectId id) {
        return collection.deleteOne(Filters.eq("_id", id));
    }

    protected abstract T documentToEntity(Document doc);
    public abstract Document entityToDocument(T entity);
}