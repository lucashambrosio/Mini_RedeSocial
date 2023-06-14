package MiniRedeSocial;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Usuario {
    private String nome;
    private String email;
    private String senha;
    private List<String> emailsAmigos;

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.emailsAmigos = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public List<String> getEmailsAmigos() {
        return emailsAmigos;
    }

    public void adicionarAmigo(String emailAmigo) {
        emailsAmigos.add(emailAmigo);
    }

    public void removerAmigo(String emailAmigo) {
        emailsAmigos.remove(emailAmigo);
    }
}

class Sistema {
    private Map<String, Usuario> usuarios;
    private Usuario usuarioAtual;
    private Connection connection;

    public Sistema() {
        this.usuarios = new HashMap<>();
        this.usuarioAtual = null;
        this.connection = null;
    }

    private void conectarAoBancoDeDados() throws SQLException {
        String url = "jdbc:postgresql://localhost/mini_RedeSocial";
        String user = "postgres";
        String password = "123456";
        connection = DriverManager.getConnection(url, user, password);
    }

    private void desconectarDoBancoDeDados() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public void cadastrarUsuario(String nome, String email, String senha) {
        try {
            conectarAoBancoDeDados();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)");
            statement.setString(1, nome);
            statement.setString(2, email);
            statement.setString(3, senha);
            statement.executeUpdate();
            statement.close();
            desconectarDoBancoDeDados();
            Usuario usuario = new Usuario(nome, email, senha);
            usuarios.put(email, usuario);
            System.out.println("Usuário cadastrado com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao cadastrar usuário!");
        }
    }

    public void login(String email, String senha) {
        try {
            conectarAoBancoDeDados();
            PreparedStatement statement = connection.prepareStatement("SELECT nome, senha FROM usuarios WHERE email = ?");
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String nome = resultSet.getString("nome");
                String senhaArmazenada = resultSet.getString("senha");
                if (senha.equals(senhaArmazenada)) {
                    Usuario usuario = new Usuario(nome, email, senha);
                    usuarioAtual = usuario;
                    System.out.println("Login efetuado com sucesso!");
                } else {
                    System.out.println("Email ou senha incorretos. Tente novamente.");
                }
            } else {
                System.out.println("Email ou senha incorretos. Tente novamente.");
            }
            resultSet.close();
            statement.close();
            desconectarDoBancoDeDados();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao efetuar login!");
        }
    }

    public void adicionarAmigo(String emailAmigo) {
        try {
            conectarAoBancoDeDados();
            PreparedStatement statement = connection.prepareStatement("SELECT nome FROM usuarios WHERE email = ?");
            statement.setString(1, emailAmigo);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String nome = resultSet.getString("nome");
                usuarioAtual.adicionarAmigo(emailAmigo);
                System.out.println("Amigo adicionado com sucesso!");
            } else {
                System.out.println("Usuário não encontrado.");
            }
            resultSet.close();
            statement.close();
            desconectarDoBancoDeDados();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao adicionar amigo!");
        }
    }

    public void removerAmigo(String emailAmigo) {
        try {
            conectarAoBancoDeDados();
            PreparedStatement statement = connection.prepareStatement("SELECT nome FROM usuarios WHERE email = ?");
            statement.setString(1, emailAmigo);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String nome = resultSet.getString("nome");
                usuarioAtual.removerAmigo(emailAmigo);
                System.out.println("Amigo removido com sucesso!");
            } else {
                System.out.println("Usuário não encontrado.");
            }
            resultSet.close();
            statement.close();
            desconectarDoBancoDeDados();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao remover amigo!");
        }
    }

    public List<String> pesquisarAmigos() {
        return usuarioAtual.getEmailsAmigos();
    }

    public void enviarMensagem(String emailAmigo, String mensagem) {
        try {
            conectarAoBancoDeDados();
            PreparedStatement statement = connection.prepareStatement("SELECT nome FROM usuarios WHERE email = ?");
            statement.setString(1, emailAmigo);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String nomeAmigo = resultSet.getString("nome");
                System.out.println("Mensagem enviada para " + nomeAmigo + ": " + mensagem);
            } else {
                System.out.println("Amigo não encontrado.");
            }
            resultSet.close();
            statement.close();
            desconectarDoBancoDeDados();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao enviar mensagem!");
        }
    }
}

public class MiniRedeSocialGUI {
    private Sistema sistema;
    private JFrame frame;
    private JTextField nomeTextField;
    private JTextField emailTextField;
    private JPasswordField senhaPasswordField;
    private JTextField emailLoginTextField;
    private JPasswordField senhaLoginPasswordField;
    private JTextField emailAmigoTextField;
    private JTextArea amigosTextArea;
    private JTextField mensagemTextField;

    public MiniRedeSocialGUI() {
        this.sistema = new Sistema();
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);


        JButton pesquisarAmigosButton = new JButton("Pesquisar amigos");
        pesquisarAmigosButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<String> amigos = sistema.pesquisarAmigos();
                StringBuilder sb = new StringBuilder();
                for (String amigo : amigos) {
                    sb.append(amigo).append("\n");
                }
                // Atualizar a área de texto "amigosTextArea" com os nomes dos amigos
                amigosTextArea.setText(sb.toString());
            }
        });
        pesquisarAmigosButton.setBounds(130, 460, 150, 30);
        frame.getContentPane().add(pesquisarAmigosButton);


        JLabel lblNewLabel = new JLabel("Nome:");
        lblNewLabel.setBounds(20, 20, 80, 20);
        frame.getContentPane().add(lblNewLabel);

        nomeTextField = new JTextField();
        nomeTextField.setBounds(110, 20, 200, 20);
        frame.getContentPane().add(nomeTextField);
        nomeTextField.setColumns(10);

        JLabel lblNewLabel_1 = new JLabel("Email:");
        lblNewLabel_1.setBounds(20, 50, 80, 20);
        frame.getContentPane().add(lblNewLabel_1);

        emailTextField = new JTextField();
        emailTextField.setBounds(110, 50, 200, 20);
        frame.getContentPane().add(emailTextField);
        emailTextField.setColumns(10);

        JLabel lblNewLabel_2 = new JLabel("Senha:");
        lblNewLabel_2.setBounds(20, 80, 80, 20);
        frame.getContentPane().add(lblNewLabel_2);

        senhaPasswordField = new JPasswordField();
        senhaPasswordField.setBounds(110, 80, 200, 20);
        frame.getContentPane().add(senhaPasswordField);

        JButton cadastrarButton = new JButton("Cadastrar");
        cadastrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nome = nomeTextField.getText();
                String email = emailTextField.getText();
                String senha = new String(senhaPasswordField.getPassword());
                sistema.cadastrarUsuario(nome, email, senha);
            }
        });
        cadastrarButton.setBounds(150, 120, 100, 30);
        frame.getContentPane().add(cadastrarButton);

        JLabel lblNewLabel_3 = new JLabel("Email:");
        lblNewLabel_3.setBounds(20, 200, 80, 20);
        frame.getContentPane().add(lblNewLabel_3);

        emailLoginTextField = new JTextField();
        emailLoginTextField.setBounds(110, 200, 200, 20);
        frame.getContentPane().add(emailLoginTextField);
        emailLoginTextField.setColumns(10);

        JLabel lblNewLabel_4 = new JLabel("Senha:");
        lblNewLabel_4.setBounds(20, 230, 80, 20);
        frame.getContentPane().add(lblNewLabel_4);

        senhaLoginPasswordField = new JPasswordField();
        senhaLoginPasswordField.setBounds(100, 230, 200, 20);
        frame.getContentPane().add(senhaLoginPasswordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = emailLoginTextField.getText();
                String senha = new String(senhaLoginPasswordField.getPassword());
                sistema.login(email, senha);
            }
        });
        loginButton.setBounds(150, 250, 100, 30);
        frame.getContentPane().add(loginButton);

        JLabel lblNewLabel_5 = new JLabel("Email do amigo:");
        lblNewLabel_5.setBounds(20, 350, 100, 20);
        frame.getContentPane().add(lblNewLabel_5);

        emailAmigoTextField = new JTextField();
        emailAmigoTextField.setBounds(130, 350, 200, 20);
        frame.getContentPane().add(emailAmigoTextField);
        emailAmigoTextField.setColumns(10);

        JButton adicionarAmigoButton = new JButton("Adicionar amigo");
        adicionarAmigoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String emailAmigo = emailAmigoTextField.getText();
                sistema.adicionarAmigo(emailAmigo);
            }
        });
        adicionarAmigoButton.setBounds(130, 380, 150, 30);
        frame.getContentPane().add(adicionarAmigoButton);

        JButton removerAmigoButton = new JButton("Remover amigo");
        removerAmigoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String emailAmigo = emailAmigoTextField.getText();
                sistema.removerAmigo(emailAmigo);
            }
        });
        removerAmigoButton.setBounds(130, 420, 150, 30);
        frame.getContentPane().add(removerAmigoButton);

        JLabel lblNewLabel_6 = new JLabel("Amigos:");
        lblNewLabel_6.setBounds(20, 150, 80, 20);
        frame.getContentPane().add(lblNewLabel_6);

        amigosTextArea = new JTextArea();
        amigosTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(amigosTextArea);
        scrollPane.setBounds(110, 150, 200, 40);
        frame.getContentPane().add(scrollPane);

        JLabel lblNewLabel_7 = new JLabel("Mensagem:");
        lblNewLabel_7.setBounds(10, 280, 100, 20);
        frame.getContentPane().add(lblNewLabel_7);

        mensagemTextField = new JTextField();
        mensagemTextField.setBounds(110, 280, 200, 20);
        frame.getContentPane().add(mensagemTextField);
        mensagemTextField.setColumns(10);

        JButton enviarMensagemButton = new JButton("Enviar mensagem");
        enviarMensagemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String emailAmigo = emailAmigoTextField.getText();
                String mensagem = mensagemTextField.getText();
                sistema.enviarMensagem(emailAmigo, mensagem);
            }


        });
        enviarMensagemButton.setBounds(110, 310, 150, 30);
        frame.getContentPane().add(enviarMensagemButton);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MiniRedeSocialGUI window = new MiniRedeSocialGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}