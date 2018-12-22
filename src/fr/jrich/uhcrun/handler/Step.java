package fr.jrich.uhcrun.handler;

import lombok.Setter;

import org.bukkit.ChatColor;

public enum Step {
    LOBBY(true, ChatColor.AQUA + "Rejoindre"),
    TELEPORTATION(false, ChatColor.RED + "En jeu"),
    IN_GAME(false, ChatColor.RED + "En jeu"),
    POST_GAME(false, ChatColor.DARK_RED + "Victoire");

    private static Step currentStep;

    public static boolean canJoin() {
        return Step.currentStep.canJoin;
    }

    public static String getMOTD() {
        return Step.currentStep.motd;
    }

    public static boolean isStep(Step step) {
        return Step.currentStep == step;
    }

    public static void setCurrentStep(Step currentStep) {
        Step.currentStep = currentStep;
    }

    public static Step getCurrentStep() {
        return Step.currentStep;
    }

    @Setter
    private boolean canJoin;

    private String motd;

    Step(boolean canJoin, String motd) {
        this.canJoin = canJoin;
        this.motd = motd;
    }
}
