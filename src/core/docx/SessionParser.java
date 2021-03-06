package core.docx;

import core.models.Deputy;
import core.models.Session;
import core.models.Vote;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionParser {

    SessionParser(){
        /*
        Перед запуском програми:
        При потребі зберегти у docx
        Замінити Мар"янович Мар"янович на Мар'янович
        Замінити "ІІІ" на "Ш"(результат помилки - 33 голоси)
        перевірити, чи дата голосування (пошуком по документу "За поправку") відділена від назви голосування
        Зменшити шрифт усього документа до 10 - для уникнення помилок, що виникають при переносі слів у таблицях
        Замінити “Голос“ на Голос
        Перевірити наявність стрічки "Голова лічильної комісії"
        !!!Не забувайте зберегти документ після форматування!!!
        * */
    }
    //
    Session getSession(String parsedSession){
        String sessionName = "";
        String sessionDate = "";
        try (Scanner scanner = new Scanner(parsedSession)) {
            //
            while (scanner.hasNextLine()){
                String tmp = scanner.nextLine();
                //
                if (tmp.contains("Дрогобицька міська рада Львівської області")) {
                    sessionName = scanner.nextLine();
                }
                //
                if (tmp.contains("РЕЗУЛЬТАТИ ПОІМЕННОГО ГОЛОСУВАННЯ")) {
                    tmp = scanner.nextLine();
                    Integer count = 0;
                    String[] allMatches = new String[2];
                    String regex = "([0-9]{2}).([0-9]{2}).([0-9]{2})\\s+([0-9]{2}):([0-9]{2}):([0-9]{2})";
                    Matcher m = Pattern.compile(regex).matcher(tmp);
                    while (m.find() && count<allMatches.length){
                        allMatches[count] = m.group();
                        tmp = allMatches[count];
                        count++;
                    }
                    sessionDate = tmp.substring(0,8);
                    break;
                }
            }
            scanner.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new Session(sessionDate, sessionName);
    }
    //
    public List<String> getParsedVoteList(String parsedSession){
        return Arrays.asList(parsedSession.split("Голова "));
    }
    //
    public Vote getVote(String parsedVote){
        String voteDate = "";
        String voteName = "";
        Map<Integer,String> namedVoting = new HashMap<>();
        try (Scanner scanner = new Scanner(parsedVote)) {
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                if (line.contains("РЕЗУЛЬТАТИ ПОІМЕННОГО ГОЛОСУВАННЯ")) {
                    line = scanner.nextLine();
                    Integer count = 0;
                    String[] allMatches = new String[2];
                    String regex = "([0-9]{2}).([0-9]{2}).([0-9]{2})\\s+([0-9]{2}):([0-9]{2}):([0-9]{2})";
                    Matcher m = Pattern.compile(regex).matcher(line);
                    while (m.find() && count<allMatches.length){
                        allMatches[count] = m.group();
                        voteDate = allMatches[count];
                        count++;
                    }
                }
                if (line.contains("“") || line.contains("\"")){
                    voteName = voteName+" "+line;
                }
                voteName = voteName.replace("\"", " ");
                voteName = voteName.replace("“", "");
                voteName = voteName.replace("“", "");
                voteName = voteName.replace("”", "");

                List<String> lst = Arrays.asList(line.split("\t"));
                Iterator<String> lst_iterator = lst.iterator();
                while (lst_iterator.hasNext()){
                    String tmp = lst_iterator.next();
                    for (Deputy deputy : DeputySingleton.getInstance().getDeputies()) {
                        String deputyFullName = deputy.getLastName()+" "+
                                deputy.getFirstName()+" "+
                                deputy.getFathersName();
                        if (tmp.equals(deputyFullName) && lst_iterator.hasNext()){
                            tmp = lst_iterator.next();
                            namedVoting.put(deputy.getId(),tmp);
                        }
                    }
                }
            }
            scanner.close();
        }
        return new Vote(voteDate,voteName,namedVoting);
    }
}
