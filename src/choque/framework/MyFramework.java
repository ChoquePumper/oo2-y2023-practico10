package choque.framework;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class MyFramework {
	private static final String nombreArchivoPropiedades = "config.properties";
	private static final String propname_acciones = "acciones";
	private List<Accion> acciones;
	private List<String> nombreclase_acciones;

	private boolean salir_del_programa;
	private Accion accion_salir;

	private Scanner scanner;

	public MyFramework() {
		procesarConfiguracion();
		prepararListaDeAcciones();

		// Crear una Accion para salir del programa: clase anónima.
		this.salir_del_programa = false;
		accion_salir = new Accion() {

			@Override
			public String nombreItemMenu() {
				return "Salir";
			}

			@Override
			public void ejecutar() {
				System.out.println("Saliendo del programa...");
				salir_del_programa = true;
			}

			@Override
			public String descripcionItemMenu() {
				return "Salir del programa";
			}
		};
	}

	private void procesarConfiguracion() {
		// Abrir y leer el archivo de configuración
		Properties config = new Properties();
		try (var f_reader = new FileReader(nombreArchivoPropiedades)) {
			config.load(f_reader);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("No se encontró el archivo de configuración: " + nombreArchivoPropiedades);
		} catch (IOException e) {
			throw new RuntimeException("Error al leer el archivo del configuración.", e);
		}

		// Leer la propiedad de acciones
		String prop_acciones = config.getProperty(propname_acciones);
		if (Objects.isNull(prop_acciones))
			throw new RuntimeException("Falta la propiedad '" + propname_acciones + "'");

		// Guardar los nombres
		nombreclase_acciones = new ArrayList<String>();
		for (String nombreclase : prop_acciones.split(";")) {
			nombreclase = nombreclase.trim();
			if (nombreclase.isEmpty())
				throw new RuntimeException("Error al parsear la propiedad '" + propname_acciones + "'");
			nombreclase_acciones.add(nombreclase);
		}
	}

	private void prepararListaDeAcciones() {
		acciones = new ArrayList<Accion>();
		for (String nombreclase : nombreclase_acciones) {
			acciones.add((Accion) instanciarClase(nombreclase));
		}
	}

	private static Object instanciarClase(String nombreclase) {
		try {
			Class<?> clase = Class.forName(nombreclase);
			return clase.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("No se pudo crear una instancia de '" + nombreclase + "'", e);
		}
	}

	public void ejecutar() {
		this.scanner = new Scanner(System.in);
		while (!salir_del_programa) {
			mostrarMenu();
			try {
				getAccion(elegirDelMenu()).ejecutar();
			} catch (OpcionInvalidaException e) {
				System.out.println(e.getMessage());
			}
		}
		this.scanner.close();
	}

	private Accion getAccion(int num) throws OpcionInvalidaException {
		if (num == 0) {
			return accion_salir;
		}
		try {
			return acciones.get(num - 1);
		} catch (IndexOutOfBoundsException e) {
			throw new OpcionInvalidaException(num);
		}

	}

	private void mostrarMenu() {
		System.out.println("Bienvenido, estas son sus opciones:\n");
		int num = 1;
		for (Accion accion : acciones) {
			mostrarOpcionDelMenu(num, accion);
			num++;
		}
		mostrarOpcionDelMenu(0, accion_salir);
		System.out.println();
	}

	private void mostrarOpcionDelMenu(int num, Accion accion) {
		System.out.printf("%d. %s (%s)\n", num, accion.nombreItemMenu(), accion.descripcionItemMenu());
	}

	private int elegirDelMenu() throws OpcionInvalidaException {
		System.out.print("Ingrese su opción: ");
		String linea = "";
		try {
			linea = scanner.nextLine();
			return Integer.parseInt(linea);
		} catch (NumberFormatException e) {
			throw new OpcionInvalidaException(linea);
		}
	}

}
