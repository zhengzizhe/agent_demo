import {Pipe} from "@angular/core";

@Pipe({
    name: 'date2',
    standalone: true
})
export class Date2Pipe {

    transform(value: number): string {
       if(!value) return ''
      // 判断是否是当年
      const isCurrentYear = new Date().getFullYear() === new Date(value).getFullYear()

      return new Date(value)
        .toLocaleDateString('zh-CN',{
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
        })  // 2024/7/24
        .replace(/\//g, '-') // 2024-7-24
        .slice(isCurrentYear ? 5 : 0) // 7-24
    }
}
