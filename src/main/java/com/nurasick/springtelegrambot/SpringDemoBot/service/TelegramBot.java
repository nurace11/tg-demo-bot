package com.nurasick.springtelegrambot.SpringDemoBot.service;

import com.nurasick.springtelegrambot.SpringDemoBot.config.BotConfig;
import com.nurasick.springtelegrambot.SpringDemoBot.entity.User;
import com.nurasick.springtelegrambot.SpringDemoBot.model.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

//@Slf4j
@Component // WebHookBot - каждый раз когда пользователь будет писать боту то бот сразу же будет уведомлен об этом (эффективен когда много пользователей)
           // LongPollingBot - сам периодически будет проверять пришли ли ему сообщения
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    final BotConfig config;
    ArrayList<User> users;

    static final String HELP_TEXT = "You are a miner." +
            "\n\nMine gold by sending 1. Sell the gold by sending 2. Upgrade your pickaxe by sending 3 (price of upgrade = [picakxe_level] * 100) " +
            " Send 4 to get info ";
    ArrayList<String> commandsList;

    public TelegramBot(BotConfig config) {
        this.config = config;
        users = new ArrayList<>();
//        users = (ArrayList<User>) userRepository.findAll();

        commandsList = new ArrayList<>(){{
            add("/start");
            add("/mydata");
            add("/deletedata");
            add("/help");
            add("/settings");
        }};

        List<BotCommand> listOfCommands = new ArrayList<>(){{
            add(new BotCommand("/start", "get a welcome message"));
            add(new BotCommand("/mydata", "get your data stored"));
            add(new BotCommand("/deletedata", "delete my data"));
            add(new BotCommand("/help", "info how to use your bot"));
            add(new BotCommand("/settings", "set your preferences"));
        }};
//        listOfCommands.add(new BotCommand("/start", "get a welcome message"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
//            log.error("Error setting bot's command list: " + e.getMessage());
            System.err.println("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    // главный метод приложения.
    static int allMessages;
    boolean oldUser;
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            User currentUser = null;
            long chatId = update.getMessage().getChatId();
            System.out.println(chatId);

            for(User u : userRepository.findAll()) {
                if(u.getChatId() == chatId){
                    currentUser = u;
                }
            }

            int status;
            String message = update.getMessage().getText();

            if(currentUser == null) {
                String msg = EmojiParser.parseToUnicode("Hello new user! Nice to meet you :blush:" + update.getMessage().getChat().getFirstName() + ". Enter:" +
                        "\n1 - копать золото " +
                        "\n2 - продать золото " +
                        "\n3 - улучшить кирку " +
                        "\n4 - посмотреть инофрмацию (баланс) ");
                sendMessage(chatId, msg);
//                currentUser = new User();
//                users.add(currentUser);
//                log.info("new user " + update.getMessage().getChat().getFirstName());
                System.out.println("new user " + update.getMessage().getChat().getFirstName());
                registerUser(update.getMessage());
            } else {



                try {
                    status = Integer.parseInt(message);
                    if(status < 0) {
                        status = -2;
                    }
                } catch (Exception e) {
                    status = -1;
                    System.out.println(EmojiParser.parseToUnicode(":blush:"));
                }

                switch (status) {
                    case -2:
                        sendMessage(chatId, EmojiParser.parseToUnicode("You can't send negative numbers :blush:"));
                        break;
                    case 0:
                        sendMessage(chatId, EmojiParser.parseToUnicode("Do not send zero :rage:"));
                        break;
                    case 1:
                        sendMessage(chatId, "You have mined " + currentUser.getPickAxePower() * 0.1 + "kg of gold");
                        currentUser.setGold(currentUser.getGold() + currentUser.getPickAxePower() * 0.1);
                        userRepository.save(currentUser);
                        break;
                    case 2:
                        sendMessage(chatId, "You have sold " + currentUser.getGold() + "kg gold, and got " + currentUser.getGold() * 1707 + "$");
                        currentUser.setDollars( (currentUser.getDollars() + (long)(currentUser.getGold() * 1707)));
                        System.out.println(currentUser.getDollars());
                        currentUser.setGold(0);
                        userRepository.save(currentUser);
                        break;
                    case 3:
                        if(currentUser.getDollars() > currentUser.getPickAxeLevel() * 100L) {
                            currentUser.setDollars(currentUser.getDollars() - currentUser.getPickAxeLevel() * 100L);

                            sendMessage(chatId,currentUser.getPickAxeLevel() * 100L + "$ was spent. " +
                                    "\n Your pickaxe power changed! " + currentUser.getPickAxePower() + " -> " + currentUser.getPickAxePower() * 1.05);

                            currentUser.setPickAxePower(currentUser.getPickAxePower() * 1.05);
                            currentUser.setPickAxeLevel(currentUser.getPickAxeLevel() + 1);

                            userRepository.save(currentUser);
                        } else {
                            sendMessage(chatId, "Not enough money. You need: " + currentUser.getPickAxeLevel() * 100L + "$");
                        }
                        break;
                    case 4:
                        sendMessage(chatId, EmojiParser.parseToUnicode(
                                ":dollar: Balance$: " + currentUser.getDollars() +
                                        "\n:yellow_circle: Gold: " + currentUser.getGold() + "" +
                                        "\n:pick:Pick axe power: " + currentUser.getPickAxePower() + "" +
                                        "\n:memo: Your data : " + currentUser.getChatId()));
                        break;
                    case -1:
                        if(messageIsCommand(message)){
                            switch (message) {
                                case "/start":
//                                sendMessage();
                                    break;
                                case "/mydata":
                                    try {
                                        sendMessage(chatId, getMe().getFirstName());
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "/help":
                                    sendMessage(chatId, HELP_TEXT);
                                    break;
                            }
                        } else {
                            sendMessage(chatId,EmojiParser.parseToUnicode("Type a number." +
                                    "\n1 - копать золото :yellow_circle:" +
                                    "\n2 - продать золото :dollar:" +
                                    "\n3 - улучшить кирку :pick:" +
                                    "\n4 - посмотреть инофрмацию (баланс) :memo:"));
                        }
                        break;
                    default:
                        sendMessage(chatId,"Nope. Type a number in [1;4]");
                        break;
                }
            }

        }




/*        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "{reset}":
                    allMessages = 0;
                    sendMessage(chatId, "Counter reset");
                    break;
                default:
                    sendMessage(chatId, messageText + " " + ++allMessages);
                    break;
            }
        }*/
    }

    private void startCommandReceived(long chatId, String name) {

        // see all emojis on emojipedia.org. Листаем до раздела shortcodes :blush: (github, slack)
        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you! " + " :blush:");
//        String answer = "Hi, " + name + ", nice to meet you!";
        System.out.println("Replied to user " + name);
//        log.info("Replied to user " + name);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error occurred " + e.getMessage());
//            log.error("Error occurred " + e.getMessage());
        }
    }

    public boolean messageIsCommand(String message) {
        for(String s : commandsList) {
            if(s.equals(message)){
                return true;
            }
        }
        return false;
    }

    public void registerUser(Message msg){
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisterDate(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
//            log.info("user saved: " + user);
            System.out.println("user saved: " + user);
        }
    }

    static Scanner scanner = new Scanner(System.in);

    public static void test(int decide) {
        switch (decide) {
            case 1:
                System.out.println("outer switch");
                decide = scanner.nextInt();
                switch (decide){
                    case 1:
                        System.out.println("inner switch");
                        break;
                }
                break;
        }
    }
}
