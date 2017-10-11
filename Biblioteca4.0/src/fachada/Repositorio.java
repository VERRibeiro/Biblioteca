package fachada;

import java.util.ArrayList;
import java.util.TreeMap;

import modelo.Autor;
import modelo.Emprestimo;
import modelo.Livro;
import modelo.Usuario;

public class Repositorio {

	private TreeMap<String,Autor> autores = new TreeMap<String,Autor>();
	private TreeMap<String,Livro> livros = new TreeMap<String,Livro>();
	
	private ArrayList<Emprestimo> emprestimos = new ArrayList<Emprestimo>();
	private ArrayList<Usuario> usuarios = new ArrayList<Usuario>();


	public ArrayList<Autor> getAutores() {
		ArrayList<Autor> aux = new ArrayList<Autor>();
		for (Autor a1: autores.values()){
			aux.add(a1);
		}
		return aux;
	}

	public ArrayList<Emprestimo> getEmprestimos() {
		return emprestimos;
	}

	public ArrayList<Livro> getLivros() {
		ArrayList<Livro> aux = new ArrayList<Livro>();
		for (Livro l: livros.values()){
			aux.add(l);
		}
		return aux;
	}

	public ArrayList<Usuario> getUsuarios() {
		return usuarios;
	}

	public void adicionarLivro (Livro l){
		livros.put(l.getTitulo(), l);
	}
	
	public void adicionarAutor (Autor a){
		autores.put(a.getNome(),a);
	}
	
	public void adicionarUsuario (Usuario u){
		usuarios.add(u);
	}
        public void adicionarEmprestimo(Emprestimo e)
        {
            emprestimos.add(e);
        }
	public void removerEmprestimo(Emprestimo e)
        {
            emprestimos.remove(e);
        }
	public Livro localizarLivro(String titulo) {
		return livros.get(titulo);
	}
	
	public ArrayList<Livro> buscarLivro(String titulo) {
		ArrayList<Livro> list =new ArrayList<Livro>();
		for(Livro l : livros.values()) {
                    String t,livro;
                    livro = l.getTitulo().toUpperCase();
                    t = titulo.toUpperCase();
                    
                    if( livro.contains(t))
                    list.add(l);
		}
		return list;
	}
	
	public ArrayList<Livro> localizarLivroAutor(String nome) {
		Autor a = autores.get(nome);			
		return a.getLivros();
	}
	
	public Autor localizarAutor(String nome){
		for(Autor a: autores.values()){
                        String autor, titulo;
                        autor = a.getNome().toUpperCase();
                        titulo = nome.toUpperCase();
			if(autor.contains(titulo)){
                            return a;
			}
		}
		return null;
	}
	
	public Usuario localizarUsuario (String email, String senha){
		for(Usuario u : usuarios){
			if(u.getEmail().equals(email)  && u.getSenha().equals(senha))
				return u;
		}
		return null;
	}
	
	public Usuario localizarUsuario(String email){
		for(Usuario u : usuarios){
			if(u.getEmail().equals(email))
				return u;
		}
		return null;
	}
        public Emprestimo localizarEmprestimo(int id, Usuario logado)//emprestimos do usuário logado
        {            
            for(Emprestimo e : logado.getEmprestimos())
            {
                if(e.getId() == id)
                    return e;
            }
            return null;
        }
	
}
