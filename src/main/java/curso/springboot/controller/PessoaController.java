package curso.springboot.controller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;
	}
	
	@GetMapping("/pessoaspaginacao")
	public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 5) Pageable pageable, ModelAndView model,
			@RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.addObject("nomepesquisa", nomepesquisa);
		model.addObject("profissoes", profissaoRepository.findAll());
		model.setViewName("cadastro/cadastropessoa");
		
		return model;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", 
			consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {
		
		pessoa.setTelefones(telefoneRepository.geTelefones(pessoa.getId()));
		
		if(bindingResult.hasErrors()) {
			ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
			andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
			andView.addObject("pessoaobj", pessoa);
			
			List<String> msgList = new ArrayList<String>();
			for(ObjectError objectError : bindingResult.getAllErrors()) {
				msgList.add(objectError.getDefaultMessage()); //vem das anotações
			}
			andView.addObject("msg", msgList);
			andView.addObject("profissoes", profissaoRepository.findAll());
			return andView;
		}
		
		//salvando curriculo
		if(file.getSize() > 0) {//salvando novo
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoFileCurriculo(file.getContentType());
			pessoa.setNomeFileCurriculo(file.getOriginalFilename());
		}else {
			if(pessoa.getId() != null && pessoa.getId() > 0) {//editando
				Pessoa pessoaTemp = pessoaRepository.findById(pessoa.getId()).get();
				pessoa.setCurriculo(pessoaTemp.getCurriculo());
				pessoa.setTipoFileCurriculo(pessoaTemp.getTipoFileCurriculo());
				pessoa.setNomeFileCurriculo(pessoaTemp.getNomeFileCurriculo());
			}
		}
		
		pessoaRepository.save(pessoa);
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView pessoas() {
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}
	
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar (@PathVariable("idpessoa") Long idpessoa) {
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", pessoa.get());
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;
	}
	
	@GetMapping("/excluirpessoa/{idpessoa}")
	public ModelAndView excluir (@PathVariable("idpessoa") Long idpessoa) {
		
		pessoaRepository.deleteById(idpessoa);
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		return andView;
	}
	
	
	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("pesqsexo") String pesqsexo, @PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {
		
		Page<Pessoa> pessoas = null;
		
		if(pesqsexo != null && !pesqsexo.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoPage(nomepesquisa, pesqsexo, pageable);
		}else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		}
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("nomepesquisa", nomepesquisa);
		andView.addObject("profissoes", profissaoRepository.findAll());
		return andView;
	}
	
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarcurriculo(@PathVariable("idpessoa") Long idpessoa, 
			HttpServletResponse response) throws IOException {
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		
		if(pessoa.getCurriculo() != null) {
			/*setar tamanho da resposta*/
			response.setContentLength(pessoa.getCurriculo().length);
			
			/*tipo do arquivo para dowload*/
			/*pode ser generica passando "application/octet-stream"*/
			response.setContentType(pessoa.getTipoFileCurriculo());
			
			/*define o cabeçalho da resposta*/
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeFileCurriculo());
			response.setHeader(headerKey, headerValue);
			
			/*finaliza a resposta passando o arquivo*/
			response.getOutputStream().write(pessoa.getCurriculo());
		}
	}
	
	@GetMapping("**/pesquisarpessoa")
	public void imprimePdf(@RequestParam("nomepesquisa") String nomepesquisa,
			@RequestParam("pesqsexo") String pesqsexo, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		
		if(pesqsexo != null && !pesqsexo.isEmpty() && nomepesquisa != null && !nomepesquisa.isEmpty()) {//busca por nome e sexo
			pessoas = pessoaRepository.findPessoaByNameSexo(nomepesquisa, pesqsexo);
		}else if (nomepesquisa != null && !nomepesquisa.isEmpty()) {//busca por nome
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
		}else if (pesqsexo != null && !pesqsexo.isEmpty()) {//busca por nome
			pessoas = pessoaRepository.findPessoaBySexo(pesqsexo);
		}else {//busca todos
			Iterable<Pessoa> iterable = pessoaRepository.findAll();
			for (Pessoa pessoa : iterable) {
				pessoas.add(pessoa);
			}
		}
		
		//chama o serviço que faz a geração do relatorio
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
		//tamanho da resposta para o navegador
		response.setContentLength(pdf.length);
		
		//definir na resposta o tipo de arquivo
		response.setContentType("application/octet-stream");
		
		//definir o cabeçalho da resposta
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headerKey, headerValue);
		
		//finaliza resposta para o navegador
		response.getOutputStream().write(pdf);
	}
	
	
	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones (@PathVariable("idpessoa") Long idpessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		
		ModelAndView andView = new ModelAndView("cadastro/telefones");
		andView.addObject("pessoaobj", pessoa.get());
		andView.addObject("telefones", telefoneRepository.geTelefones(idpessoa));
		return andView;
	}
	
	@PostMapping("**/addfonePessoa/{pessoaid}")
	public ModelAndView addfonePessoa(Telefone telefone, @PathVariable("pessoaid") Long pessoaid) {
		
		Pessoa pessoa = pessoaRepository.findById(pessoaid).get();
		telefone.setPessoa(pessoa);
		
		telefoneRepository.save(telefone);
		
		ModelAndView andView = new ModelAndView("cadastro/telefones");
		andView.addObject("pessoaobj", pessoa);
		andView.addObject("telefones", telefoneRepository.geTelefones(pessoaid));
		return andView;
	}
	
	@GetMapping("/excluirtelefone/{idtelefone}")
	public ModelAndView excluirTelefone (@PathVariable("idtelefone") Long idtelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idtelefone);
		
		ModelAndView andView = new ModelAndView("cadastro/telefones");

		andView.addObject("telefones", telefoneRepository.geTelefones(pessoa.getId()));
		andView.addObject("pessoaobj", pessoa);
		return andView;
	}
	
}
