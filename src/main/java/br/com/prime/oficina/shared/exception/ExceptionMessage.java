package br.com.prime.oficina.shared.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionMessage {

    public static final String CUSTOMER_NOT_FOUND = "Cliente não encontrado";
    public static final String VEHICLE_NOT_FOUND = "Veículo não encontrado";
    public static final String SERVICE_ORDER_NOT_FOUND = "Ordem de serviço não encontrada";
    public static final String ITEM_NOT_FOUND = "Item não encontrado";
    public static final String SERVICE_NOT_FOUND = "Servico não encontrado";
    public static final String EXISTING_CUSTOMER = "Já existe cliente cadastrado com este CPF/CNPJ";
    public static final String DUPLICATED_VEHICLE = "Já existe veículo cadastrado com esta placa";
    public static final String ITEM_STOCK_ERROR = "Estoque do item não encontrado";
    public static final String SAME_ITEM_ERROR = "Já existe item cadastrado com o mesmo tipo, descrição e unidade de medida";
    public static final String NOT_ACTIVE_CUSTOMER = "O cliente informado não está ativo";
    public static final String NOT_ACTIVE_VEHICLE = "O Veículo informado não está ativo";
    public static final String NOT_ACTIVE_ITEM = "O Item informado não está ativo";
    public static final String NOT_ACTIVE_SERVICE =  "O Serviço informado não está ativo";
    public static final String NOT_ENOUGH_STOCK_ITEM = "Estoque insuficiente para o item: ";
    public static final String STARTED_OR_FINISHED_SERVICE = "Serviço já iniciado ou finalizado";
    public static final String FINISHED_OR_CANCELED_SERVICE = "Serviço finalizado ou cancelado";
    public static final String EXISTING_SERVICE = "Já existe serviço cadastrado com este nome";
    public static final String EXISTING_USER_SAME_EMAIL = "Já existe usuário cadastrado com este email";
    public static final String INVALID_PERSON_DOCUMENT = "CPF inválido";
    public static final String INVALID_COMPANY_DOCUMENT = "CNPJ inválido";
    public static final String INVALID_VEHICLE_PLATE = "Placa inválida, não segue o padrão.";


}
