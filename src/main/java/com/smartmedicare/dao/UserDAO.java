package com.smartmedicare.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.smartmedicare.models.User;
import com.smartmedicare.utils.DatabaseConnection;
import com.smartmedicare.utils.DatabaseConfig;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO implements BaseDAO<User> {
    private final MongoCollection<User> collection;

    public UserDAO() {
        this.collection = DatabaseConnection.getInstance()
            .getDatabase()
            .getCollection(DatabaseConfig.USERS_COLLECTION, User.class);
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            user.setId(new ObjectId());
        }
        collection.insertOne(user);
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            return Optional.ofNullable(collection.find(Filters.eq("_id", new ObjectId(id))).first());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        collection.find().into(users);
        return users;
    }

    @Override
    public void update(User user) {
        collection.replaceOne(
            Filters.eq("_id", user.getId()),
            user,
            new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void delete(String id) {
        DeleteResult result = collection.deleteOne(Filters.eq("_id", id));
        if (result.getDeletedCount() == 0) {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(collection.find(Filters.eq("username", username)).first());
    }

    public Optional<User> findByUsernameAndType(String username, User.UserType userType) {
        return Optional.ofNullable(collection.find(
            Filters.and(
                Filters.eq("username", username),
                Filters.eq("userType", userType.toString())
            )
        ).first());
    }
}