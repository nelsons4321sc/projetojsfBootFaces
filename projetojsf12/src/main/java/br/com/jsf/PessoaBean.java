package br.com.jsf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

import br.com.dao.DAOGeneric;
import br.com.entidades.Cidades;
import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.jpautil.JPAUtil;
import br.com.repository.IDaoPessoa;
import br.com.repository.IDaoPessoaImpl;

@ViewScoped
@ManagedBean(name = "pessoaBean")
public class PessoaBean {
	
		// O manageBean é quem controla a tela JSF
		//è necessário um modelo para instanciar a classe	
		private Pessoa pessoa = new Pessoa();
		private Estados estado = new Estados();
		
		private DAOGeneric<Pessoa> daoGeneric = new DAOGeneric<Pessoa>();
		// pra carregar uma lista de pessoas é preciso ter um list de pessoas
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		private IDaoPessoa iDaoPessoa = new IDaoPessoaImpl();
		
		private List<SelectItem> estados;
		
		private List<SelectItem> cidades;
		
		private Part arquivoFoto;		

		public Pessoa getPessoa() {
			return pessoa;
		}

		public void setPessoa(Pessoa pessoa) {
			this.pessoa = pessoa;
		}
		
		public List<Pessoa> getPessoas() {
			return pessoas;
		}
		
		public void setPessoas(List<Pessoa> pessoas) {
			this.pessoas = pessoas;
		}
		
		public Estados getEstado() {
			return estado;
		}
		
		public void setEstado(Estados estado) {
			this.estado = estado;
		}
		
		public List<SelectItem> getCidades() {
			return cidades;
		}
		
		public void setCidades(List<SelectItem> cidades) {
			this.cidades = cidades;
		}
		
		public Part getArquivoFoto() {
			return arquivoFoto;
		}
		
		public void setArquivoFoto(Part arquivoFoto) {
			this.arquivoFoto = arquivoFoto;
		}
		
		public String salvar()  throws Exception{
			
			//processar a imagem
			byte[] imagemByte = getByte(arquivoFoto.getInputStream());
			pessoa.setFotoIconBase64Original(imagemByte);//salva imagem original
			
			//transformar em BufferImagem
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagemByte));
			
			//pega o tipo da imagem
			int type = bufferedImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType();
			
			int largura = 200;
			int altura = 200;
			
			//criar a miniatura
			BufferedImage resizeImage = new BufferedImage(largura, altura, type);
			Graphics2D g = resizeImage.createGraphics();
			g.drawImage(bufferedImage, 0, 0, largura, altura, null);
			g.dispose();
			
			//Escrever novamente a imagem
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String extensao = arquivoFoto.getContentType().split("\\/")[1];  //retorna /*image/png*/
			ImageIO.write(resizeImage, extensao, baos);
			
			String  miniImagem = "data:"+ arquivoFoto.getContentType() + ";base64," +
			DatatypeConverter.printBase64Binary(baos.toByteArray());
								
			
			pessoa.setFotoIconBase64(miniImagem);
			pessoa.setExtensão(extensao);
								
			pessoa = daoGeneric.merge(pessoa);
			carregarPessoas();
			mostrarmsg("Cadastro com sucesso");
			return "";
		}
		
		private void mostrarmsg(String msg) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage(msg);
			context.addMessage(null, message);
			
		}

		public String novo() {
			pessoa = new Pessoa();
			return "";
		}
		
		public String remove() {
			daoGeneric.removerporId(pessoa);
			pessoa = new Pessoa();
			carregarPessoas();
			mostrarmsg("Removido com sucesso");
			return "";
		}
		//anotação para manter dados na tela quando a página é carregada
		@PostConstruct
		public void carregarPessoas() {
			// Atribuido a lista de pessoas a consulta de dados que feita no BD
			pessoas = daoGeneric.getListEntity(Pessoa.class);
		}
		
		public String logar() {
			
			Pessoa pessoaUser = iDaoPessoa.consultarUsuario(pessoa.getLogin(), pessoa.getSenha());
			
			if (pessoaUser != null) { //achou o usuário
				//adicionar o usuário na sessão que no caso é o usuarioLogado que está na classe FilterAutenticacao
				FacesContext context = FacesContext.getCurrentInstance();
				ExternalContext externalContext = context.getExternalContext();
				externalContext.getSessionMap().put("usuarioLogado", pessoaUser);
				
				mostrarmsg("Bem-Vindo");
				return "cadastro";
				//return "primeiraPagina";
			}else {
				 FacesContext.getCurrentInstance().addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Usuário inválido ou senha incorreta", null));
		         //return "index";
			}
			return "index.jsf";
		}
		
		public boolean permiteAcesso(String acesso) {
			
			//recuperado o objeto da sessão
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
			//através do get atribuindo a variável pessoa
			Pessoa pessoaUser = (Pessoa) externalContext.getSessionMap().get("usuarioLogado");
			
			return pessoaUser.getPerfilUser().equals(acesso);
		}
		
		public String limpar() {
			pessoa = new Pessoa();
			// retorno que permite, permanecer na mesma tela
			return "";
		}
		
		public void pesquisacep(AjaxBehaviorEvent event) {
			
			try {
				URL url = new URL("https://viacep.com.br/ws/"+pessoa.getCep()+"/json/");
				URLConnection connection = url.openConnection();
				InputStream is = connection.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				
				String cep = "";
				StringBuilder jsonCep = new StringBuilder();
				
				while ((cep = br.readLine()) != null) {
					jsonCep.append(cep);
				}
				
				Pessoa gsonAux = new Gson().fromJson(jsonCep.toString(), Pessoa.class);
				
				pessoa.setCep(gsonAux.getCep());
				pessoa.setLogradouro(gsonAux.getLogradouro());
				pessoa.setComplemento(gsonAux.getComplemento());
				pessoa.setBairro(gsonAux.getBairro());
				pessoa.setLocalidade(gsonAux.getLocalidade());
				pessoa.setUf(gsonAux.getUf());
						
				
			
			} catch (Exception e) {
				e.printStackTrace();
				mostrarmsg("Erro ao consultar o CEP");
			}
		
		}
		
		public String deslogar () {
			
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
			externalContext.getSessionMap().remove("usuarioLogado");
			
			HttpServletRequest httpServletRquest = (HttpServletRequest)
					context.getCurrentInstance().getExternalContext().getRequest();
			
			httpServletRquest.getSession().invalidate();
			
			return "index";
			
		}
		
		public List<SelectItem> getEstados() {
			estados = iDaoPessoa.listaEstados();
			return estados;
		}
		
		
		
		public void carregarCidades(AjaxBehaviorEvent event) {
			
			//Antes do converter
			//String codigoEstado = (String) event.getComponent().getAttributes().get("submittedValue");
			//depois das implemntações do converter
			Estados estado = (Estados) ((HtmlSelectOneMenu) event.getSource()).getValue();
			
			//if(codigoEstado != null) {
			
				//Estados estado = JPAUtil.getEntityManager()
					//	.find(Estados.class, Long.parseLong(codigoEstado));
				if(estado != null) {
					pessoa.setEstados(estado);
					
					List<Cidades> cidades = JPAUtil.getEntityManager() 
							.createQuery("from Cidades where estados.id = "
							+estado.getId()).getResultList();
					
					List<SelectItem> selectItemsCidades = new ArrayList<SelectItem>();
					
					for (Cidades cidade : cidades) {
						selectItemsCidades.add(new SelectItem(cidade, cidade.getNome()));
					}
					setCidades(selectItemsCidades);
				}
			//}
		}
		
		public void editar( ) {
			if(pessoa.getCidades() != null) {
				Estados estado = pessoa.getCidades().getEstados();
				pessoa.setEstados(estado);
				
				List<Cidades> cidades = JPAUtil.getEntityManager() 
						.createQuery("from Cidades where estados.id = "
						+estado.getId()).getResultList();
				
				List<SelectItem> selectItemsCidades = new ArrayList<SelectItem>();
				
				for (Cidades cidade : cidades) {
					selectItemsCidades.add(new SelectItem(cidade, cidade.getNome()));
				}
				setCidades(selectItemsCidades);
			}
		}
		//metodo que converte inputstream para array de bytes
			private byte[] getByte(InputStream is) throws Exception {
				
				int len;
				int size = 1024;
				byte[] buf = null;
				if(is instanceof ByteArrayInputStream) {
					
					size = is.available();
					buf = new byte[size];
					len = is.read(buf, 0, size);
				} else {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					buf = new byte[size];
					
					while((len = is.read(buf, 0, size)) != -1) {
						bos.write(buf, 0 , len);
					}
					
					buf = bos.toByteArray();
				}
				return buf;
			}
}
