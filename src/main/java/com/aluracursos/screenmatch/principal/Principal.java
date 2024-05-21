package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.Serierepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=a955e4f3";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();

    private Serierepository repositorio;
    private List<Serie> series;
    Optional<Serie> serieBuscada;

    public Principal(Serierepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - buscar series por titulo
                    5 - Top 5 mejores series
                    6 - Buscar series por categoria
                    7 - Filtrar series
                    8 - Buscar episodios por titulo
                    9 - Top 5 episodios por serie
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTirulo();
                    break;
                case 5:
                    buscartop5Series();
                    break;
                case 6:
                    buscaarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        //DatosSerie datosSerie = getDatosSerie();
        mostrarSeriesBuscadas();
        System.out.println("Escribe le nombre de la serie, para ver los episodios");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie =series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }


    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        // guardar datos en la base de datos
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        // transformación al tipo de datos series
//        List<Serie> series = new ArrayList<>();
//        series = datosSeries.stream()
//                        .map(d -> new Serie(d))
//                        .collect(Collectors.toList());
        // para visualizar las series gardadas en la base de datos
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void  buscarSeriePorTirulo() {
        System.out.println("Escribe le nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();

        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);

        if (serieBuscada.isPresent()) {
            System.out.println("La serie buscada es:" +serieBuscada.get());
        }else {
            System.out.println("Serie No encontrada");
        }
    }

    private void buscartop5Series() {
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s ->
                System.out.println("Serie: "+ s.getTitulo() + " Evaluación: " + s.getEvaluacion()) );
    }

    private void buscaarSeriesPorCategoria() {
        System.out.println("Escriba el genero/categoria de la seria que desea buscar: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);

        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las Series de la categoria "+ genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void filtrarSeriesPorTemporadaYEvaluacion() {
        System.out.println("¿Filtrar series con cuántas temporadas?");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Con evaluación a partir de cúal valor?");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();

        List<Serie> filtroSerie = repositorio.seriesPorTemporadaYEvaluacion(totalTemporadas, evaluacion);
        System.out.println("*** Series filtradas ***");
        filtroSerie.forEach(s ->
                System.out.println(s.getTitulo() + " -Evacualión: "+ s.getEvaluacion()));
    }

    public void buscarEpisodiosPorTitulo(){
        System.out.println("Escribe el nombre del episodio que desea buscar");
        var nombreEpisodio = teclado.nextLine();

        List<Episodio> episodiosEncontrados = repositorio.episodiosporNombre(nombreEpisodio);

        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s Temporada: %s Episodio: %s Evaluación: %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getEvaluacion()));
    }

    public void buscarTop5Episodios() {
        buscarSeriePorTirulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();

            List<Episodio> topEpisodios = repositorio.top5Episodios(serie);

            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s " +
                                    "Temporada: %s Episodio: %s Evaluación: %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()));
        }

    }


}

