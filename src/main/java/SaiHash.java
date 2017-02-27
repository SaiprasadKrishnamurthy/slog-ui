/**
 * The SaiHash algorithm was created by Austin Appleby and placed in the public domain.
 * This java port was authored by Yonik Seeley and also placed into the public domain.
 * The author hereby disclaims copyright to this source code.
 * <p>
 * This produces exactly the same hash values as the final C++
 * version of SaiHash and is thus suitable for producing the same hash values across
 * platforms.
 * <p>
 * The 32 bit x86 version of this hash should be the fastest variant for relatively short keys like ids.
 * murmurhash3_x64_128 is a good choice for longer strings or if you need more than 32 bits of hash.
 * <p>
 * Note - The x86 and x64 versions do _not_ produce the same results, as the
 * algorithms are optimized for their respective platforms.
 * <p>
 * See http://github.com/yonik/java_util for future updates to this file.
 */

import org.apache.ibatis.jdbc.SQL;

public final class SaiHash {


    public static void main(String[] args) throws Exception {

        String sql = new SQL() {{
            SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME");
            SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON");
            FROM("PERSON P");
            FROM("ACCOUNT A");
            INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID");
            INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID");
            WHERE("P.ID = A.ID");
            WHERE("P.FIRST_NAME like ?");
            OR();
            WHERE("P.LAST_NAME like ?");
            GROUP_BY("P.ID");
            HAVING("P.LAST_NAME like ?");
            OR();
            HAVING("P.FIRST_NAME like ?");
            ORDER_BY("P.ID");
            ORDER_BY("P.FULL_NAME");
        }}.toString();
        System.out.println(sql);
    }
}