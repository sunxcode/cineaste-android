package de.cineaste.android.database.dbHelper;

import android.content.Context;

import de.cineaste.android.database.CineasteDatabase;
import de.cineaste.android.database.dao.UserDao;
import de.cineaste.android.entity.User;

public class UserDbHelper {

	private static UserDbHelper instance;

	private final UserDao userDao;

	private UserDbHelper(Context context) {
		userDao = CineasteDatabase.getInstance(context).getUserDao();
	}

	public static UserDbHelper getInstance(Context context) {
		if (instance == null) {
			instance = new UserDbHelper(context);
		}

		return instance;
	}


	public void createUser(User user) {
		userDao.create(user);
	}

	public User getUser() {
		return userDao.getUser();
	}
}