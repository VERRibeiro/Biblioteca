package fachada;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import modelo.Administrador;
import modelo.Aluno;
import modelo.Autor;
import modelo.Emprestimo;
import modelo.Funcionario;
import modelo.Livro;
import modelo.Usuario;

public class Fachada {
	private static Usuario logado = null;
	private static Repositorio biblioteca = new Repositorio();
	private static int id = 0;
	private static int flag = 0;
	
	// Métodos referentes ao login 

	public static Usuario cadastrarUsuario(String nome, String email, String senha,String titulo,int cod) throws  Exception{                
                
                if(logado == null && flag == 1)
                    throw new Exception("Administrador precisa estar logado");
                Usuario u = biblioteca.localizarUsuario(email);   
                if (u!=null)
			throw new Exception("Pessoa ja cadastrado:" + nome);
                if(flag == 0)
                {
                    flag = 1;
                    u = new Administrador(nome,email,senha,titulo);
                    if(u instanceof Administrador)
                    biblioteca.adicionarUsuario(u);
                }                           		
                else if(logado instanceof Administrador)
                {
                    if(cod == 1)
                    u = new Administrador(nome,email,senha,titulo);
                    else if(cod == 2)
                        u = new Funcionario(nome,email,senha,titulo);
                    else if(cod == 3)
                        u = new Aluno(nome,email,senha,titulo);
                    biblioteca.adicionarUsuario(u);
                    
                }else                    
                    throw new Exception("Somente administradores podem cadastrar usuários");                
                
		return u;
	}
	public static String listarUsuarios()
        {
            String buffer = "";
            for(Usuario u : biblioteca.getUsuarios())
            {
                if(u instanceof Aluno)
                    buffer+="Aluno :"+ u.toString()+"\n";
                else
                    buffer+="Funcionário: "+ u.toString() +"\n";
            }
            return buffer;
        }
	
	public static ArrayList<Usuario> pegarUsuarios (){
		return biblioteca.getUsuarios();
	}
	
	public static void login(String email, String senha) throws Exception{		
		if(logado!=null) throw new Exception ("ja existe uma pessoa logada");
		
		Usuario u = biblioteca.localizarUsuario(email,senha);
		if(u==null)
			throw new Exception ("login nao foi possivel por email ou senha invalida:"+email);
		else
			logado = u;
	}
	public static void excluirUsuario(String email) throws Exception
        {            
            Usuario u = biblioteca.localizarUsuario(email);
            if(u instanceof Administrador)
            {
                 for(Emprestimo e : u.getEmprestimos())
                {
                    if(e.getDatadev() == null)
                        throw new Exception("O usuario precisa devolver todos os livros para ser excluído");
                }
                for(Emprestimo e: u.getEmprestimos())
                {
                    e.getLivro().removerEmprestimo(e);
                    e.getUsuario().removerEmprestimo(e);
                    biblioteca.removerEmprestimo(e);
                    e.setLivro(null);
                    e.setUsuario(null);                
                }
            }else            
                throw new Exception("Somente Administradores nao podem excluir usuarios");
           
        }
	public static void logoff() {		
		logado = null;
	}

	public static Usuario getLogado(){
		return logado;
	}
	// Fim login

	
	public static List<Livro> listarLivros (){
		return biblioteca.getLivros();
	} 
	
	public static ArrayList<Livro> busca (String nome) throws Exception {
		ArrayList<Livro> l = biblioteca.buscarLivro(nome);
		if (l.isEmpty()) throw new Exception("Nenhum livro encontrado!");
		return l;
	}
	
	public static ArrayList<Livro> buscaPorAutor (String nome) throws Exception{ 
		// Se o autor não existir
		if (biblioteca.localizarLivroAutor(nome) == null) throw new Exception("Este autor não foi localizado!");
		return biblioteca.localizarLivroAutor(nome);
	}
	
	public static ArrayList<Emprestimo> listarEmprestimos (){
		return biblioteca.getEmprestimos();
	}
	
	public static void cadastrarLivro(String titulo, ArrayList<String> autores, int qtd) throws Exception{
		
		Livro l = biblioteca.localizarLivro(titulo);
		// Se ja existir
		if( l != null )throw new Exception ("Este Livro ja existe.");
		//Verifica autor duplicado
                for(int i = 0; i < autores.size() - 1; i++)
                {
                    for(int j = i + 1; j < autores.size(); j++)
                    {
                        if(autores.get(i).equals(autores.get(j))) throw new Exception("Autor duplicado");
                    }
                };
		// Cria um novo livro
		l = new Livro(titulo, qtd);
		biblioteca.adicionarLivro(l);
		for(String autor : autores){
			if(biblioteca.localizarAutor(autor) == null){ // Verificar se o autor existe, senão cria
				Autor a = new Autor (autor);
				biblioteca.adicionarAutor(a);
			}
			Autor a = biblioteca.localizarAutor(autor);
			a.addLivro(l);
            l.adicionarAutor(a);
		}
	}
	
	public static Emprestimo criarEmprestimo(String titulo) throws Exception
        {            
            if (logado==null) 
		throw new Exception("Operacao somente para logados");
            Livro livro = biblioteca.localizarLivro(titulo);
            if(livro == null)
                throw new Exception("Livro não existe");
            titulo = livro.getTitulo();
            if(livro.getQuantidade() == 0)             
               throw new Exception("Livro não possui exemplares disponíveis"); 
            Emprestimo verificar;
            verificar = biblioteca.localizarEmprestimo(id, logado);
            if(verificar != null)
            {
                if(verificar.getLivro().getTitulo().equals(titulo))
                   throw new Exception("Usuário não pode locar mais de uma vez o mesmo livro");                          
            }
            id++;            
            Emprestimo e = new Emprestimo(id,logado,livro);                                  
            biblioteca.adicionarEmprestimo(e);
            livro.setQuantidade(livro.getQuantidade() - 1);
            livro.adicionaEmprestimo(e);            
            logado.addEmprestimo(e);
            return e;
	}
        
	public static Emprestimo criarDevolucao(int id) throws Exception{
            if (logado==null) 
		throw new Exception("Operacao somente para logados");
            Emprestimo e = biblioteca.localizarEmprestimo(id,logado);
            if(e == null)
                throw new Exception("Id não corresponde a um emprestimo válido");
            else if(e.getDatadev()!= null)
                throw new Exception("Livro foi devolvido");
            else
            {
                LocalDate devo = e.getDataemp();
                double multa = 0;
                int prazo = logado.getPrazo();
                if(logado instanceof Aluno)   
                {
                    devo.plusDays(prazo);
                    long days = ChronoUnit.DAYS.between(LocalDate.now(),devo);   
                    if(days > prazo)
                        multa = days - prazo;
                }
                else if(logado instanceof Funcionario)
                {
                    devo.plusDays(prazo);
                    long days = ChronoUnit.DAYS.between(LocalDate.now(),devo);   
                    if(days > prazo)
                        multa = days - prazo;
                }else if(logado instanceof Administrador)
                e.setMulta(multa);
                e.setDatadev(devo);               
                Livro l = e.getLivro();
                l.setQuantidade(l.getQuantidade()+1);
                l.removerEmprestimo(e);                
                e.setUsuario(null);                
            }
            return e;
	}
	
	public static ArrayList<Emprestimo> listarMeusEmprestimos() throws Exception{
		if(logado == null) throw new Exception ("Você não está logado");
		return logado.getEmprestimos();
	} 

}
