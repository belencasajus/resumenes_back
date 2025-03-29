package com.resumen.isst.resumenes.model;

public enum RolUsuario {
    VISITANTE,
    LECTOR,
    ADMIN;

    public static RolUsuario getRolUsuario(int i) {
        return RolUsuario.values()[i];
    }
}
