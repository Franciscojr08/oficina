package br.com.prime.oficina.shared.validator;

public class ValidadorCPF {

	private ValidadorCPF() {}

	public static boolean isValido(String cpf) {
		if (cpf.matches("(\\d)\\1{10}")) {
			return false;
		}

		int soma = 0;
		for (int i = 0; i < 9; i++) {
			soma += (cpf.charAt(i) - '0') * (10 - i);
		}

		int digito1 = 11 - (soma % 11);
		digito1 = (digito1 >= 10) ? 0 : digito1;

		soma = 0;
		for (int i = 0; i < 10; i++) {
			soma += (cpf.charAt(i) - '0') * (11 - i);
		}

		int digito2 = 11 - (soma % 11);
		digito2 = (digito2 >= 10) ? 0 : digito2;

		return digito1 == (cpf.charAt(9) - '0') && digito2 == (cpf.charAt(10) - '0');
	}
}
