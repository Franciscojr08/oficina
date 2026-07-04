package br.com.prime.oficina.shared.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionMessage {

    public static final String CUSTOMER_NOT_FOUND = "Cliente não encontrado";
    public static final String VEHICLE_NOT_FOUND = "Veículo não encontrado";
    public static final String SERVICE_ORDER_NOT_FOUND = "Ordem de serviço não encontrada";
    public static final String ITEM_NOT_FOUND = "Item não encontrado";
    public static final String SERVICE_NOT_FOUND = "Serviço não encontrado";
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
	public static final String INVALID_STATUS_FOR_MODIFICATION = "Status inválido para alteração";
	public static final String SERVICE_NOT_FOUND_FOR_ORDER = "Serviço não encontrado para essa ordem de serviço";
	public static final String VEHICLE_DOES_NOT_BELONG_TO_CUSTOMER = "O veículo informado não pertence ao cliente informado.";
	public static final String CANNOT_ADD_ITEM_WITH_ORDER_OUTSIDE_DIAGNOSIS = "Não é possível adicionar o item, pois a ordem de serviço não está %s";
	public static final String CANNOT_ADD_SERVICE_WITH_ORDER_OUTSIDE_DIAGNOSIS = "Não é possível adicionar o serviço, pois a ordem de serviço não está %s";
	public static final String INVALID_ORDER_STATUS_FOR_ACTION = "Não é possível %s, pois a ordem de serviço está %s";
	public static final String SERVICE_ORDER_MUST_HAVE_ITEM_AND_SERVICE_TO_REQUEST_APPROVAL = "A OS deve possuir ao menos uma peça e um serviço para solicitar aprovação";
	public static final String CANNOT_INACTIVATE_CUSTOMER_WITH_ACTIVE_SERVICE_ORDERS = "Não é possível inativar o cliente, pois ele possui ordens de serviço ativas.";
	public static final String CANNOT_INACTIVATE_VEHICLE_WITH_ACTIVE_SERVICE_ORDERS = "Não é possível inativar o veículo, pois ele possui ordens de serviço ativas.";
	public static final String CANNOT_INACTIVATE_SERVICE_WITH_ACTIVE_SERVICE_ORDERS = "Não é possível inativar o serviço, pois ele possui ordens de serviço ativas.";
	public static final String CANNOT_INACTIVATE_ITEM_WITH_ACTIVE_SERVICE_ORDERS = "Não é possível inativar o item, pois ele possui ordens de serviço ativas.";
	public static final String SERVICE_IN_EXECUTION_IN_SERVICE_ORDER = "Serviço em execução em ordem de serviço";
}
