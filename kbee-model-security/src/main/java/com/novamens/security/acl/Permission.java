package com.novamens.security.acl;

import java.io.Serializable;

public interface Permission extends Serializable {
    public boolean equals(Object another);
    public String toString();
}
