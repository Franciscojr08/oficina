package br.com.prime.oficina.shared.validator;

public class ValidadorPlaca {

	private  ValidadorPlaca() {}

	private static final String PADRAO_ANTIGO = "^[A-Z]{3}\\d{4}$";
	private static final String PADRAO_MERCOSUL = "^[A-Z]{3}\\d[A-Z]{2}\\d{2}$";

	public static boolean isValida(String placa) {
		if (placa == null) {
			return false;
		}

		String valor = placa.toUpperCase().replaceAll("[^A-Z0-9]", "");

		return valor.matches(PADRAO_ANTIGO) || valor.matches(PADRAO_MERCOSUL);
	}
}
