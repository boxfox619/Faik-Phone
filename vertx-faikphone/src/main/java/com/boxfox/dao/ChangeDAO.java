package com.boxfox.dao;


import java.sql.*;
import java.util.Random;

/**
 * Created by dsm_025 on 2017-03-30.
 */
public class ChangeDAO extends DAO {
    private Connection connection = null;

    /**
     * RealPhone 에서 토큰을 등록할 때
     *
     * @param token
     * @return
     */
    public boolean insertRealPhoneToken(String token, String phoneNum) {
        String sql = "insert into conn(realtoken, code, pnum) values (?, ?, ?) ";
        String code = createRandomCode();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, token);
            preparedStatement.setString(2, code);
            preparedStatement.setString(3, phoneNum);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Faik Phone 에서 인증 성공 후 토큰을 등록할 때
     *
     * @param token
     * @param code
     * @return
     */
    public boolean insertFaikPhoneToken(String token, String code) {
        if (isAnotherFakePhoneRegisterd(code)) {            //이미 다른 FakePhone이 등록 되어 있는지 검사
            return false;
        }

        String sql = "update conn set faketoken = '" + token + "' where code = '" + code + "'";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * FaikPhone과의 Connetion을 해제 하는 메소드
     *
     * @param token
     * @param state : true = realPhone, false = fakePhone
     * @return
     */
    public boolean resetConnection(String token, boolean state) {
        String sql = "update conn set faketoken = '' where";
        sql += state ? " realtoken = '" + token + "'" : "faketoken = '" + token + "'";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 모든 정보를 reset하는 메소드 ( 폰 Mode를 변경 할 때 )
     *
     * @param token
     * @param state : true = realPhone, false = fakePhone
     * @return
     */
    public boolean resetAll(String token, boolean state) {
        if (isTokenRegister(token)) {
            String sql = state == true ? "delete from conn where realtoken = '" + token + "'" : "delete from conn where faketoken = '" + token + "'";
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.execute();
            } catch (SQLException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 인증 코드를 reset하는 메소드
     *
     * @param token
     * @return
     */
    public boolean resetCode(String token) {
        String code = createRandomCode();
        String sql = "update conn set code = '" + code + "' where realtoken = '" + token + "'";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * fakeToken에 맞는 Conn 객체를 반환하는 메소드
     *
     * @param fakeToken
     * @return
     */
    public Conn getConnFromFakeToken(String fakeToken) {
        try {
            ResultSet rs = selectResultSetFromFakeToken(fakeToken);
            return new Conn(rs.getString("realtoken"), rs.getString("code"), rs.getString("faketoken"), rs.getString("pnum"));
        } catch (SQLException e) {
        }
        return null;
    }

    /**
     * realToken에 맞는 Conn 객체를 반환하는 메소드
     *
     * @param realToken
     * @return
     */
    public Conn getConnFromRealToken(String realToken) {
        try {
            ResultSet rs = selectResultSetFromRealToken(realToken);
            rs.next();
            return new Conn(rs.getString("realtoken"), rs.getString("code"), rs.getString("faketoken"), rs.getString("pnum"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Conn getConnFromCode(String code) {
        try {
            ResultSet rs = selectResultSetFromCode(code);
            rs.next();
            return new Conn(rs.getString("realtoken"), rs.getString("code"), rs.getString("faketoken"), rs.getString("pnum"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * realPhone의 토큰의 ResultSet을 반환하는 메소드
     *
     * @param realToken
     * @return
     * @throws SQLException
     */
    public ResultSet selectResultSetFromRealToken(String realToken) throws SQLException {
        String sql = "select * from conn where realtoken = '" + realToken + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        return rs;
    }

    /**
     * fakePhone의 토큰의 ResultSet을 반환하는 메소드
     *
     * @param fakeToken
     * @return
     * @throws SQLException
     */
    public ResultSet selectResultSetFromFakeToken(String fakeToken) throws SQLException {
        String sql = "select * from conn where faketoken = '" + fakeToken + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        return rs;
    }

    /**
     * code에 해당하는 ResultSet을 반환하는 메소드
     *
     * @param code
     * @return
     * @throws SQLException
     */
    public ResultSet selectResultSetFromCode(String code) throws SQLException {
        String sql = "select * from conn where code = '" + code + "'";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        return rs;
    }

    /**
     * 인증 코드가 올바른지 체크하는 메소드
     *
     * @param code
     * @return
     */
    private boolean isCodeValid(String code) {
        try {
            ResultSet rs = selectResultSetFromCode(code);
            if (rs.getString("code").equals(code)) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    /**
     * RealPhone 토큰이 등록 되어 있는지를 체크하는 메소드
     *
     * @param realToken
     * @return
     */
    private boolean isTokenRegister(String realToken) {
        return getConnFromRealToken(realToken) != null;
    }

    private boolean isAnotherFakePhoneRegisterd(String code) {
        return !getConnFromCode(code).getFakeToken().equals("");
    }

    /**
     * 인증 코드를 create 하는 메소드
     *
     * @return code
     */
    private String createRandomCode() {
        String code = "";
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            char engCode = (char) ((int) (Math.random() * 25) + 65);
            int numCode = (int) (Math.random() * 10);
            code = code + ((random.nextInt(2) == 1) ? engCode : numCode + "");
        }
        return code;
    }

    /**
     * 인증 코드를 얻어 오는 메소드
     *
     * @return AuthCode
     */
    public String getAuthCode(String realToken) {
        try {
            ResultSet resultSet = selectResultSetFromRealToken(realToken);
            resultSet.next();
            return resultSet.getString("code");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPhoneNum(String fakeToken) {
        try {
            ResultSet resultSet = selectResultSetFromFakeToken(fakeToken);
            return resultSet.getString("pnum");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void bind(Connection connection) {
        this.connection = connection;
    }

    public class Conn {
        private String realToken;
        private String fakeToken;
        private String code;
        private String pnum;

        public Conn(String realToken, String code, String fakeToken, String pnum) {
            this.realToken = realToken;
            this.fakeToken = fakeToken;
            this.code = code;
            this.pnum = pnum;
        }

        public String getRealToken() {
            return realToken;
        }

        public String getFakeToken() {
            return fakeToken;
        }

        public String getCode() {
            return code;
        }

        public String getPnum() {
            return pnum;
        }

        public void setFakeToken(String fakeToken) {
            this.fakeToken = fakeToken;
        }

        public void setRealToken(String realToken) {
            this.realToken = realToken;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public void setPnum(String pnum) {
            this.pnum = pnum;
        }
    }
}
