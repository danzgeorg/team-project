package com.novasolutions.ipospu.db;

import com.novasolutions.ipospu.model.Member;

public class MemberDAOTestMain {
    public static void main(String[] args) {
        MemberDAO memberDAO = new MemberDAO();
        Member member = memberDAO.findByEmail("test@example.com");
        boolean member1 = memberDAO.emailExists("test@example.com");

        if (member != null) {
            System.out.println("✅ Member found:");
            System.out.println(member.fullName() + " | " + member.email()); // I updated this from name() to fullName() to match the renamed field
        } else {
            System.out.println("❌ Member not found");
        }

        if (member1) {
            System.out.println("✅ Email exists in the database");
        } else {
            System.out.println("❌ Email does not exist in the database");
        }
    }
}