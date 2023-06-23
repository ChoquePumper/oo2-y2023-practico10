package choque.framework;

public class OpcionInvalidaException extends Exception {
	private static final long serialVersionUID = -626995644159115971L;

	public OpcionInvalidaException(Object seleccion) {
		super("Opcion invalida: " + seleccion.toString());
	}
}
