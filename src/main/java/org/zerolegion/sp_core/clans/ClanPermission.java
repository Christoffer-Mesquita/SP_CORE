package org.zerolegion.sp_core.clans;

public enum ClanPermission {
    // Gerenciamento de Membros
    INVITE_MEMBER("Convidar membros"),
    KICK_MEMBER("Expulsar membros"),
    SET_MEMBER_ROLE("Definir cargos"),
    
    // Base do Clã
    SET_BASE("Definir base"),
    TELEPORT_BASE("Teleportar para base"),
    
    // Banco do Clã
    BANK_DEPOSIT("Depositar no banco"),
    BANK_WITHDRAW("Sacar do banco"),
    
    // Alianças e Guerras
    MANAGE_ALLIES("Gerenciar alianças"),
    DECLARE_WAR("Declarar guerra"),
    
    // Configurações do Clã
    SET_TAG("Alterar tag"),
    SET_DESCRIPTION("Alterar descrição"),
    SET_ANNOUNCEMENT("Alterar anúncio"),
    MANAGE_SETTINGS("Gerenciar configurações"),
    
    // Eventos do Clã
    START_EVENT("Iniciar eventos"),
    END_EVENT("Encerrar eventos");

    private final String description;

    ClanPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 