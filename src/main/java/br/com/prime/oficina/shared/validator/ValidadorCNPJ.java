package br.com.prime.oficina.shared.validator;

public class ValidadorCNPJ {

	public static boolean isValido(String cnpj) {
		if (cnpj.matches("(\\d)\\1{13}")) {
			return false;
		}

		int[] peso1 = {5,4,3,2,9,8,7,6,5,4,3,2};
		int[] peso2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};

		int soma = 0;
		for (int i = 0; i < 12; i++) {
			soma += (cnpj.charAt(i) - '0') * peso1[i];
		}

		int digito1 = soma % 11;
		digito1 = (digito1 < 2) ? 0 : 11 - digito1;

		soma = 0;
		for (int i = 0; i < 13; i++) {
			soma += (cnpj.charAt(i) - '0') * peso2[i];
		}

		int digito2 = soma % 11;
		digito2 = (digito2 < 2) ? 0 : 11 - digito2;

		return digito1 == (cnpj.charAt(12) - '0') && digito2 == (cnpj.charAt(13) - '0');
	}
}
