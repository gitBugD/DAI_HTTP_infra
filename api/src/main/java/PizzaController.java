import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
class PizzaController {
    private ConcurrentHashMap<Integer, Pizza> pizzas = new ConcurrentHashMap<Integer, Pizza>();
    private int lastId = 0;
    public PizzaController() {
        pizzas.put(++lastId, new Pizza("Margherita", new ArrayList<>(List.of("pomodoro", "mozzarella", "basilico"))));
        pizzas.put(++lastId, new Pizza("Buffalo Bill", new ArrayList<>(List.of("pomodoro", "bufala", "basilico"))));
        pizzas.put(++lastId, new Pizza("Veggy", new ArrayList<>(List.of("pomodoro", "mozzarella", "verdure"))));
        pizzas.put(++lastId, new Pizza("4 Stagioni", new ArrayList<>(List.of("pomodoro", "mozzarella", "prosciutto", "funghi", "olive", "carciofini"))));
        pizzas.put(++lastId, new Pizza("4 Formaggi", new ArrayList<>(List.of("mozzarella", "stracchino", "gorgonzola", "toma"))));
        pizzas.put(++lastId, new Pizza("Sfida", new ArrayList<>(List.of("pomodoro", "mozzarella", "gorgonzola", "cipolle"))));
    }
    public void getOne(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ctx.json(pizzas.get(id));
        }
        catch (Exception ex){
            ctx.status(404);
        }
    }

    public void getAll(Context ctx) {
        ctx.json(pizzas);
    }
    public void create(Context ctx) {
        Pizza pizza = ctx.bodyAsClass(Pizza.class);
        pizzas.put(++lastId, pizza);
        ctx.status(201);
    }
    public void delete(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        pizzas.remove(id);
        ctx.status(204);
    }
    public void update(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Pizza pizza = ctx.bodyAsClass(Pizza.class);
        pizzas.put(id, pizza);
        ctx.status(200);
    }

}