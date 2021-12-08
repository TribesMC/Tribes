package me.rey.core.utils;

import me.rey.Main;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class Text {

    public static String format(String prefix, String message) {
        prefix = Text.color(String.format("&9%s »&r", prefix));
        message = Text.color(message);
        return (prefix + " " + message);
    }

    public static String color(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text.replaceAll("&g", "&a").replaceAll("&s", "&e").replaceAll("&r", "&7").replaceAll("&q", "&c&l").replaceAll("&w", "&a&l"));
    }

    public static void log(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', "&d[" + Main.getPlugin(Main.class).getName() + "]&r " + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void debug(final String msg) {
        log("&7[&eDEBUG&7]&r" + msg);
    }

    public static String format(final String text) {
        final String[] name = text.replaceAll("_", " ").toLowerCase().split(" ");
        final StringBuilder message = new StringBuilder();
        for (String s : name) {
            s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            message.append(s).append(" ");
        }
        return message.toString().trim();
    }

    public static boolean hasColor(final String text) {
        return !text.equals(ChatColor.stripColor(text));
    }

    public static boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public static double eval(final String str) {
        return eval("N/A", str);
    }

    public static double eval(final String cause, final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                this.ch = (++this.pos < str.length()) ? str.charAt(this.pos) : -1;
            }

            boolean eat(final int charToEat) {
                while (this.ch == ' ') {
                    this.nextChar();
                }
                if (this.ch == charToEat) {
                    this.nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                this.nextChar();
                final double x = this.parseExpression();
                if (this.pos < str.length()) {
                    throw new RuntimeException("Cause: " + cause + " | Unexpected: " + (char) this.ch);
                }
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = this.parseTerm();
                for (; ; ) {
                    if (this.eat('+')) {
                        x += this.parseTerm(); // addition
                    } else if (this.eat('-')) {
                        x -= this.parseTerm(); // subtraction
                    } else {
                        return x;
                    }
                }
            }

            double parseTerm() {
                double x = this.parseFactor();
                for (; ; ) {
                    if (this.eat('*')) {
                        x *= this.parseFactor(); // multiplication
                    } else if (this.eat('/')) {
                        x /= this.parseFactor(); // division
                    } else {
                        return x;
                    }
                }
            }

            double parseFactor() {
                if (this.eat('+')) {
                    return this.parseFactor(); // unary plus
                }
                if (this.eat('-')) {
                    return -this.parseFactor(); // unary minus
                }

                double x;
                final int startPos = this.pos;
                if (this.eat('(')) { // parentheses
                    x = this.parseExpression();
                    this.eat(')');
                } else if ((this.ch >= '0' && this.ch <= '9') || this.ch == '.') { // numbers
                    while ((this.ch >= '0' && this.ch <= '9') || this.ch == '.') {
                        this.nextChar();
                    }
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (this.ch >= 'a' && this.ch <= 'z') { // functions
                    while (this.ch >= 'a' && this.ch <= 'z') {
                        this.nextChar();
                    }
                    final String func = str.substring(startPos, this.pos);
                    x = this.parseFactor();
                    if (func.equals("sqrt")) {
                        x = Math.sqrt(x);
                    } else if (func.equals("sin")) {
                        x = Math.sin(Math.toRadians(x));
                    } else if (func.equals("cos")) {
                        x = Math.cos(Math.toRadians(x));
                    } else if (func.equals("tan")) {
                        x = Math.tan(Math.toRadians(x));
                    } else {
                        throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) this.ch);
                }

                if (this.eat('^')) {
                    x = Math.pow(x, this.parseFactor()); // exponentiation
                }

                return x;
            }
        }.parse();
    }

    public static String formatName(final String text) {
        final String[] name = text.replaceAll("_", " ").toLowerCase().split(" ");
        final StringBuilder message = new StringBuilder();
        for (String s : name) {
            s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            message.append(s).append(" ");
        }
        return message.toString().trim();
    }

}
