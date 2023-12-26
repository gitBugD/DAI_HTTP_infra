import io.javalin.*;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7000);
        PizzaController pizzaController = new PizzaController();
        app.get("/api", ctx -> ctx.result("Welcome to Pizza API!"));
        app.get("/api/pizzas", pizzaController::getAll);
        app.get("/api/pizzas/{id}", pizzaController::getOne);
        app.post("/api/pizzas", pizzaController::create);
        app.put("/api/pizzas/{id}", pizzaController::update);
        app.delete("/api/pizzas/{id}", pizzaController::delete);
    }
}