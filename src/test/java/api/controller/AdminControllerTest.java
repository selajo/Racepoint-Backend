package api.controller;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminControllerTest extends AdminController {

    void deleteAll() {
        while(this.lastLoginAttempts.size() > 0) {
            this.lastLoginAttempts.remove(0);
        }
    }

    void fillAll() {
        while(this.lastLoginAttempts.size() < 6) {
            this.lastLoginAttempts.add(new Date());
        }
    }

    @Test
    public void notTooManyLogins_ok() {
        deleteAll();
        assertTrue(this.notTooManyLoginAttempts());
    }

    @Test
    public void notTooManyLogins_nok() {
        fillAll();
        assertFalse(this.notTooManyLoginAttempts());
    }
}
