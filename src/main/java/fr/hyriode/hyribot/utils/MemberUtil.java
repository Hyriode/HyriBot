package fr.hyriode.hyribot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MemberUtil {

    public static List<Member> getMemberByNameOrId(Guild guild, String idOrName) {
        if(idOrName == null)
            return new ArrayList<>();
        String[] split = idOrName.split("#");
        if(split.length > 0) {
            String end = split[split.length - 1];
            if (end.matches("\\d+") && end.length() == 4) {
                return Collections.singletonList(guild.getMemberByTag(idOrName));
            }
        }

        if(idOrName.matches("\\d+")) {
            return Collections.singletonList(guild.getMemberById(idOrName));
        }
        return guild.getMembers().stream().filter(member -> member.getUser().getName().toLowerCase().contains(idOrName.toLowerCase())).collect(Collectors.toList());
    }

}
