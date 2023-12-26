import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pizza {
    public String name = "";
    public ArrayList<String> ingredients = new ArrayList<>();
    public Pizza() { }
    public Pizza(String name, ArrayList<String> ingredients){
        this.name = name;
        addIngredients(ingredients);
    }
    public void ingredients(ArrayList<String> ingredients){
        this.ingredients.clear();
        addIngredients(ingredients);
    }
    public void addIngredients(ArrayList<String> ingredients){
        this.ingredients.addAll(ingredients);
    }
    public void removeIngredients(ArrayList<String> ingredients){
        this.ingredients.removeAll(ingredients);
    }
    public void name(String  name){
        this.name = name;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Pizza ").append(name).append(": ");
        for (String i : ingredients) {
            sb.append(i).append(" ");
        }
        return sb.toString();
    }

    // to test Pizza class
    public static void main(String... args){
        Pizza margherita = new Pizza("Margherita", new ArrayList<>(List.of("pomodoro", "mozzarella", "basilico")));
        System.out.println(margherita);

        Pizza giancarlo = new Pizza("Giancarlo", new ArrayList<>(List.of("pomodoro", "mozzarella", "pesto")));
        System.out.println(giancarlo);

        margherita.name("Regina");
        margherita.addIngredients(new ArrayList<>(List.of("burrata")));
        margherita.removeIngredients(new ArrayList<>(List.of("mozzarella")));
        System.out.println(margherita);

        Pizza veggy = new Pizza("Veggy", new ArrayList<>(List.of("pomodoro", "mozzarella", "peperoni", "zucchine", "melanzane")));
        System.out.println(veggy);

        veggy.ingredients(new ArrayList<>(List.of("pomodoro", "mozzarella", "olive", "spinaci", "zucchine", "melanzane", "funghi", "cipolla")));
        System.out.println(veggy);
    }
}
