package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class ClearService {
    private final DataAccess dataAccess;

    public ClearService(DataAccess data) {
        dataAccess = data;
    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clear();
    }
}
