package com.cartagenacorp.lm_users.util;

public class ConstantUtil {

    private ConstantUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PERMISSION_DENIED = "No tiene permisos para realizar esta acción";
    public static final String DATA_INTEGRITY_FAIL_MESSAGE = "Problemas con la integridad de los datos";
    public static final String RESOURCE_NOT_FOUND = "Recurso no encontrado";
    public static final String INVALID_INPUT = "Entrada inválida";
    public static final String INVALID_UUID = "El ID proporcionado no es un UUID válido";
    public static final String DUPLICATE_ROLE = "El rol ya existe";
    public static final String DUPLICATE_PERMISSION= "El permiso ya existe";
    public static final String INTERNAL_SERVER_ERROR = "Error interno del servidor";

    public class Success {

        public static final String ROLE_CREATED = "Rol creado correctamente";
        public static final String ROLE_UPDATED = "Rol actualizado correctamente";
        public static final String ROLE_DELETED = "Rol eliminado correctamente";
        public static final String PERMISSION_DELETED = "Permiso eliminado correctamente";

        public Success() {
            throw new IllegalStateException("Util class");
        }

    }

}
