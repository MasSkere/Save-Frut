const fs=require('fs');
const p=require('path');
function w(d){
  fs.readdirSync(d).forEach(e=>{
    const f=p.join(d,e);
    const s=fs.statSync(f);
    if(s.isDirectory()) w(f);
    else if(f.endsWith('.java')){
      let c=fs.readFileSync(f,'utf8');
      let n=c.split('com.savefrut.api.usuarios').join('com.savefrut.msidentidad');
      if(n!==c){ fs.writeFileSync(f,n); }
    }
  });
}
w('C:/Users/ronad/Desktop/SaveFrut/backend/ms-identidad/src');
console.log('done');
