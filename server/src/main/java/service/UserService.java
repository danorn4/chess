package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;
import servicehelpers.LoginRequest;


public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
        if(user.username() == null || user.email() == null || user.password() == null) {
            throw new DataAccessException("bad request");
        } if(dataAccess.getUser(user.username()) != null){
            throw new DataAccessException("already taken");
        }

        dataAccess.createUser(user);

        return dataAccess.createAuth(user.username());
    }

    public AuthData login(LoginRequest request) throws DataAccessException {
        if(request.username() == null || request.password() == null){
            throw new DataAccessException("bad request");
        }

        UserData user = dataAccess.getUser(request.username());

        if(user == null || !BCrypt.checkpw(request.password(), user.password())){
            throw new DataAccessException("unauthorized");
        }

        return dataAccess.createAuth(user.username());
    }

    public void logout(String authToken) throws DataAccessException {
        if(authToken == null){
            throw new DataAccessException("bad request");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if(auth == null){
            throw new DataAccessException("unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        return auth;
    }
}
